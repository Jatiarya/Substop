const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_substop_123";
const BOB_UID = "bob_substop_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write profiles or subscriptions", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("subscriptions").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("subscriptions").doc("sub_1").set({
      userId: ALICE_UID,
      name: "Netflix",
      amount: 15.99,
      cadence: "MONTHLY",
      category: "Entertainment",
      status: "ACTIVE",
      nextBillingEpochDay: 20778,
      brandColorHex: "#E50914",
      iconKey: "movie",
      reminderEnabled: true,
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Authenticated owner can create and update UserProfile and Subscription", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      email: "alice@example.com",
      monthlyBudget: 200.0,
      renewalAlertsEnabled: true,
      trialWarningsEnabled: true,
      weeklyDigestEnabled: false,
      currencySymbol: "$",
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      monthlyBudget: 250.0,
      weeklyDigestEnabled: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Create subscription mirroring Kotlin payload (no @DocumentId `id` field)
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("subscriptions").doc("sub_netflix").set({
      userId: ALICE_UID,
      name: "Netflix Premium",
      amount: 22.99,
      monthlyCost: 22.99,
      cadence: "MONTHLY",
      category: "Entertainment",
      status: "ACTIVE",
      nextBillingDate: "Nov 25, 2026",
      nextBillingEpochDay: 20780,
      brandColorHex: "#E50914",
      iconKey: "movie",
      paymentLabel: "Visa •• 4242",
      notes: "4K family plan",
      reminderEnabled: true,
      createdAt: created,
      updatedAt: created,
    })
  );

  // List subcollection (even when empty or populated)
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("subscriptions").get()
  );

  // Partial status update
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("subscriptions").doc("sub_netflix").update({
      status: "PAUSED",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Delete subscription
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("subscriptions").doc("sub_netflix").delete()
  );
});

test("Rejects ghost fields, @DocumentId id field, tampered timestamps, and invalid enums", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);

  // Reject `id` field in payload
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("subscriptions").doc("sub_bad_id").set({
      id: "sub_bad_id",
      userId: ALICE_UID,
      name: "Spotify",
      amount: 11.99,
      cadence: "MONTHLY",
      category: "Entertainment",
      status: "ACTIVE",
      nextBillingEpochDay: 20780,
      brandColorHex: "#1DB954",
      iconKey: "music",
      reminderEnabled: true,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Reject ghost field
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("subscriptions").doc("sub_ghost").set({
      userId: ALICE_UID,
      name: "Spotify",
      amount: 11.99,
      cadence: "MONTHLY",
      category: "Entertainment",
      status: "ACTIVE",
      nextBillingEpochDay: 20780,
      brandColorHex: "#1DB954",
      iconKey: "music",
      reminderEnabled: true,
      isAdmin: true,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Reject future timestamp
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("subscriptions").doc("sub_future").set({
      userId: ALICE_UID,
      name: "Spotify",
      amount: 11.99,
      cadence: "MONTHLY",
      category: "Entertainment",
      status: "ACTIVE",
      nextBillingEpochDay: 20780,
      brandColorHex: "#1DB954",
      iconKey: "music",
      reminderEnabled: true,
      createdAt: new Date(Date.now() + 60000),
      updatedAt: created,
    })
  );

  // Reject invalid cadence enum
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("subscriptions").doc("sub_enum").set({
      userId: ALICE_UID,
      name: "Spotify",
      amount: 11.99,
      cadence: "DAILY",
      category: "Entertainment",
      status: "ACTIVE",
      nextBillingEpochDay: 20780,
      brandColorHex: "#1DB954",
      iconKey: "music",
      reminderEnabled: true,
      createdAt: created,
      updatedAt: created,
    })
  );
});

test("Cross-user isolation prevents reading or modifying another user's data", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).collection("subscriptions").doc("bob_sub").set({
      userId: BOB_UID,
      name: "iCloud+",
      amount: 2.99,
      cadence: "MONTHLY",
      category: "Cloud & AI",
      status: "ACTIVE",
      nextBillingEpochDay: 20780,
      brandColorHex: "#3B82F6",
      iconKey: "cloud",
      reminderEnabled: true,
      createdAt: new Date(Date.now() - 10000),
      updatedAt: new Date(Date.now() - 10000),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("subscriptions").get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("subscriptions").doc("bob_sub").get());
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("subscriptions").doc("bob_sub").update({
      status: "CANCELLED",
      updatedAt: new Date(Date.now() - 500),
    })
  );
});
