package org.ergoplatform;

import org.junit.Assume;
import org.junit.Before;

public class ApiTestBase {

    /**
     * These tests run against the live Ergo network (public nodes and explorer),
     * so they are inherently dependent on network availability and current
     * blockchain state. They are skipped by default and can be enabled with
     * {@code sbt -Dergo.test.integration=true test}.
     */
    @Before
    public void assumeIntegrationTestsEnabled() {
        Assume.assumeTrue(
            "Skipping integration tests against the live network, " +
            "enable with -Dergo.test.integration=true",
            Boolean.getBoolean("ergo.test.integration"));
    }

    public String address = "9hHDQb26AjnJUXxcqriqY1mnhpLuUeC81C4pggtK7tupr92Ea1K";

    /** NFT: Blockchain whales #02 */
    public String tokenId = "7970bba36de5d4d6b9c24c20dc65d764ebf7d4aeb5eeb01bbe94221aa03fee45";

    public String blockId = "78a76fb6c8ac11e7e9da01f2c916b82dd1220a370d7fcfe2df94caa675c21926";
    public String txFromTheBlockId = "24f6996bea6b914d3dab7d645cd5e5b9a57e3ac88b2774d34a2be26bdf708d28";

    public String ergoTree = "0008cd036ba5cfbc03ea2471fdf02737f64dbcd58c34461a7ec1e586dcd713dacbf89a12";

    public String boxId = "a7e33955a4d84fcfdfe9268a2950ad897772209a1f84fec9f206731918ac57d6";

    public String txId = "95faa1a6582adb7e5d482cdfd765ea63b92fc13c18538f1211f6e551e9c38875";

}
