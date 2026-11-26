const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ADMIN_UID = "admin_pucuz999";
const ADMIN_EMAIL = "pucuz999@gmail.com";
const CUSTOMER_A_UID = "cust_alice_123";
const CUSTOMER_A_EMAIL = "alice@example.com";
const CUSTOMER_B_UID = "cust_bob_456";
const CUSTOMER_B_EMAIL = "bob@example.com";

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

function buildCustomerSessionPayload(uid, email, displayName, ts) {
  return {
    userId: uid,
    displayName,
    email,
    loginMode: "CUSTOMER",
    role: "customer",
    accessEnabled: true,
    blockedReason: "",
    sessionActive: true,
    currentScreen: "Boost Hub",
    deviceModel: "Motorola Edge 50 Pro",
    activePresetName: "Motorola Edge 50 Pro (Scribd Auto)",
    lastActiveAt: ts,
    createdAt: ts,
    updatedAt: ts,
  };
}

test("Unauthenticated users: cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const now = new Date(Date.now() - 1000);

  await assertFails(unauthDb.collection("user_sessions").get());
  await assertFails(unauthDb.collection("user_sessions").doc(CUSTOMER_A_UID).get());
  await assertFails(
    unauthDb
      .collection("user_sessions")
      .doc(CUSTOMER_A_UID)
      .set(buildCustomerSessionPayload(CUSTOMER_A_UID, CUSTOMER_A_EMAIL, "Alice", now))
  );
  await assertFails(unauthDb.collection("app_config").doc("global").get());
  await assertFails(unauthDb.collection("admins").doc(ADMIN_UID).get());
});

test("Customer: can create own session, update own presence/screen, and query own session", async () => {
  const aliceDb = testEnv
    .authenticatedContext(CUSTOMER_A_UID, { email: CUSTOMER_A_EMAIL, email_verified: true })
    .firestore();
  const created = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb
      .collection("user_sessions")
      .doc(CUSTOMER_A_UID)
      .set(buildCustomerSessionPayload(CUSTOMER_A_UID, CUSTOMER_A_EMAIL, "Alice Gamer", created))
  );

  await assertSucceeds(
    aliceDb.collection("user_sessions").doc(CUSTOMER_A_UID).update({
      displayName: "Alice Gamer",
      email: CUSTOMER_A_EMAIL,
      loginMode: "CUSTOMER",
      sessionActive: true,
      currentScreen: "Touch Calibrator",
      deviceModel: "Motorola Edge 50 Pro",
      activePresetName: "One-Tap 144Hz",
      lastActiveAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Scoped owner list query matches Kotlin rule parity
  await assertSucceeds(
    aliceDb.collection("user_sessions").where("userId", "==", CUSTOMER_A_UID).get()
  );

  // Unfiltered list query must be rejected for non-admins
  await assertFails(aliceDb.collection("user_sessions").get());
});

test("Customer security bounds: blocks privilege escalation, self-unblock, PII cross-read, and ghost fields", async () => {
  const now = new Date(Date.now() - 5000);
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db
      .collection("user_sessions")
      .doc(CUSTOMER_A_UID)
      .set({
        ...buildCustomerSessionPayload(CUSTOMER_A_UID, CUSTOMER_A_EMAIL, "Alice", now),
        accessEnabled: false,
        blockedReason: "Suspended by Admin",
      });
    await db
      .collection("user_sessions")
      .doc(CUSTOMER_B_UID)
      .set(buildCustomerSessionPayload(CUSTOMER_B_UID, CUSTOMER_B_EMAIL, "Bob", now));
  });

  const aliceDb = testEnv
    .authenticatedContext(CUSTOMER_A_UID, { email: CUSTOMER_A_EMAIL, email_verified: true })
    .firestore();

  // Cannot read another customer's PII
  await assertFails(aliceDb.collection("user_sessions").doc(CUSTOMER_B_UID).get());

  // Cannot unblock self
  await assertFails(
    aliceDb.collection("user_sessions").doc(CUSTOMER_A_UID).update({
      accessEnabled: true,
      blockedReason: "",
      lastActiveAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Cannot escalate own role to admin
  await assertFails(
    aliceDb.collection("user_sessions").doc(CUSTOMER_A_UID).update({
      role: "admin",
      loginMode: "ADMIN",
      lastActiveAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Cannot include @DocumentId `id` field or future timestamp
  await assertFails(
    aliceDb.collection("user_sessions").doc(CUSTOMER_B_UID).set({
      ...buildCustomerSessionPayload(CUSTOMER_B_UID, CUSTOMER_B_EMAIL, "Bob", now),
      id: CUSTOMER_B_UID,
    })
  );
  await assertFails(
    aliceDb.collection("user_sessions").doc(CUSTOMER_A_UID).update({
      currentScreen: "Boost Hub",
      lastActiveAt: new Date(Date.now() + 60000),
      updatedAt: new Date(Date.now() + 60000),
    })
  );
});

test("Email spoofing defense: unverified admin email cannot list or control users", async () => {
  const spoofDb = testEnv
    .authenticatedContext("spoof_uid", { email: ADMIN_EMAIL, email_verified: false })
    .firestore();

  await assertFails(spoofDb.collection("user_sessions").get());
  await assertFails(
    spoofDb.collection("app_config").doc("global").set({
      customersAllowed: false,
      maintenanceMessage: "Hacked",
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Verified Admin: can list all sessions, toggle customer access, manage app_config, and promote admins", async () => {
  const now = new Date(Date.now() - 5000);
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx
      .firestore()
      .collection("user_sessions")
      .doc(CUSTOMER_A_UID)
      .set(buildCustomerSessionPayload(CUSTOMER_A_UID, CUSTOMER_A_EMAIL, "Alice", now));
  });

  const adminDb = testEnv
    .authenticatedContext(ADMIN_UID, { email: ADMIN_EMAIL, email_verified: true })
    .firestore();

  // Admin creates own admin session
  await assertSucceeds(
    adminDb.collection("user_sessions").doc(ADMIN_UID).set({
      userId: ADMIN_UID,
      displayName: "System Admin",
      email: ADMIN_EMAIL,
      loginMode: "ADMIN",
      role: "admin",
      accessEnabled: true,
      blockedReason: "",
      sessionActive: true,
      currentScreen: "Admin Console",
      deviceModel: "Pixel 9 Pro",
      activePresetName: "Admin Mode",
      lastActiveAt: now,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Admin can list all active/registered user sessions
  await assertSucceeds(adminDb.collection("user_sessions").get());

  // Admin can suspend a customer and set reason
  await assertSucceeds(
    adminDb.collection("user_sessions").doc(CUSTOMER_A_UID).update({
      accessEnabled: false,
      blockedReason: "Trial session paused by admin",
      updatedAt: new Date(Date.now() - 1000),
      lastActiveAt: new Date(Date.now() - 1000),
    })
  );

  // Admin can configure global customer access policy
  await assertSucceeds(
    adminDb.collection("app_config").doc("global").set({
      customersAllowed: true,
      maintenanceMessage: "Server maintenance in progress.",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Customer can read global access policy, but cannot update it
  const aliceDb = testEnv
    .authenticatedContext(CUSTOMER_A_UID, { email: CUSTOMER_A_EMAIL, email_verified: true })
    .firestore();
  await assertSucceeds(aliceDb.collection("app_config").doc("global").get());
  await assertFails(
    aliceDb.collection("app_config").doc("global").update({
      customersAllowed: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Admin can promote another user in /admins
  await assertSucceeds(
    adminDb.collection("admins").doc(CUSTOMER_B_UID).set({
      uid: CUSTOMER_B_UID,
      email: CUSTOMER_B_EMAIL,
      grantedBy: ADMIN_EMAIL,
      createdAt: now,
      updatedAt: now,
    })
  );
});
