package org.ergoplatform.appkit;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import scala.Tuple2;
import special.collection.Coll;

/**
 * Represents an EIP-4 compliant token.
 * <p>
 * See https://github.com/ergoplatform/eips/blob/master/eip-0004.md
 */
public class Eip4Token extends ErgoToken {
    // required EIP-4 fields from R4, R5, R6
    private final String name;
    private final String description;
    private final int decimals;

    // optional, type defines the content. We keep this as an ErgoValue to be compatible with
    // future types.
    private final ErgoValue<?> r7;
    private final ErgoValue<?> r8;
    private final ErgoValue<?> r9;

    public Eip4Token(@Nonnull String id, long amount, @Nonnull ErgoValue<Coll<Object>> r4,
                     @Nonnull ErgoValue<Coll<Object>> r5, @Nonnull ErgoValue<Coll<Object>> r6,
                     @Nullable ErgoValue<?> r7, @Nullable ErgoValue<?> r8, @Nullable ErgoValue<?> r9) {
        this(id, amount,
            new String(JavaHelpers$.MODULE$.collToByteArray(r4.getValue()), StandardCharsets.UTF_8),
            new String(JavaHelpers$.MODULE$.collToByteArray(r5.getValue()), StandardCharsets.UTF_8),
            Integer.parseInt(new String(JavaHelpers$.MODULE$.collToByteArray(r6.getValue()), StandardCharsets.UTF_8)),
            r7, r8, r9);
    }

    public Eip4Token(@Nonnull String id, long amount, @Nonnull String name,
                     @Nonnull String description, int decimals) {
        this(id, amount, name, description, decimals, null, null, null);
    }

    public Eip4Token(@Nonnull String id, long amount, @Nonnull String name,
                     @Nonnull String description, int decimals,
                     @Nullable ErgoValue<?> r7, @Nullable ErgoValue<?> r8, @Nullable ErgoValue<?> r9) {
        super(id, amount);
        this.name = name;
        this.description = description;
        this.decimals = decimals;

        // these can be nullable, but either all of them or R7 and R8 defined
        if (!(r7 != null && r8 != null || r7 == null && r8 == null && r9 == null)) {
            throw new IllegalArgumentException("Either define all of R7 to R9 or none of them");
        }

        this.r7 = r7;
        this.r8 = r8;
        this.r9 = r9;
    }

    /**
     * @return EIP-4 token verbose name (R4)
     */
    public String getTokenName() {
        return name;
    }

    /**
     * @return EIP-4 token description (R5)
     */
    public String getTokenDescription() {
        return description;
    }

    /**
     * @return EIP-4 token decimals (R6)
     */
    public int getDecimals() {
        return decimals;
    }

    /**
     * @return token amount taking decimals into account
     */
    public String getAmountFormatted() {
        return BigDecimal.valueOf(getValue()).movePointLeft(decimals).toPlainString();
    }

    public AssetType getAssetType() {
        byte[] assetType = getR7ByteArrayOrNull();
        if (r7 == null) {
            return AssetType.NONE;
        } else if (assetType == null) {
            return AssetType.UNKNOWN;
        } else {
            for (AssetType type : AssetType.values()) {
                byte[] r7EntryForType = type.getR7ByteArrayForType();

                if (r7EntryForType != null && Arrays.equals(r7EntryForType, assetType))
                    return type;
            }

            return AssetType.UNKNOWN;
        }
    }

    private byte[] getR7ByteArrayOrNull() {
        // r7 specifies the asset type and should be Coll<Byte>
        if (r7 != null && r7.getValue() instanceof Coll) {
            Coll<?> assetType = (Coll<?>) r7.getValue();

            if (assetType.length() > 0 && assetType.apply(0) instanceof Byte) {
                return JavaHelpers$.MODULE$.collToByteArray((Coll<Object>) r7.getValue());
            } else {
                return null;
            }

        } else {
            return null;
        }
    }

    /**
     * @return true if this is an NFT token asset type according to EIP-4
     */
    public boolean isNftAssetType() {
        byte[] assetType = getR7ByteArrayOrNull();
        return (assetType != null && assetType.length == 2 && assetType[0] == 1);
    }

    /**
     * @return Sha256 content hash for NFT types, or null for non-NFT types
     */
    public byte[] getNftContentHash() {
        if (isNftAssetType()) {
            return JavaHelpers$.MODULE$.collToByteArray((Coll<Object>) r8.getValue());
        } else {
            return null;
        }
    }

    /**
     * @return content link for NFT types if available, otherwise null
     */
    public String getNftContentLink() {
        if (r9 != null && isNftAssetType()) {
            // if r9 ErgoValue is Coll[Byte], we have the direct link here
            if (r9.getValue() instanceof Coll) {
                return new String(JavaHelpers$.MODULE$.collToByteArray((Coll<Object>) r9.getValue()),
                    StandardCharsets.UTF_8);
            } else if (r9.getValue() instanceof Tuple2) {
                return new String(JavaHelpers$.MODULE$.collToByteArray((Coll<Object>) ((Tuple2) r9.getValue())._1),
                    StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    /**
     * @return cover image link for NFT types if available, otherwise null
     */
    public String getNftCoverImageLink() {
        if (r9 != null && isNftAssetType() && r9.getValue() instanceof Tuple2) {
            return new String(JavaHelpers$.MODULE$.collToByteArray((Coll<Object>) ((Tuple2) r9.getValue())._2),
                StandardCharsets.UTF_8);
        } else {
            return null;
        }
    }

    public ErgoValue<Coll<scala.Byte>> getMintingBoxR4() {
        return ErgoValue.of(name.getBytes(StandardCharsets.UTF_8));
    }

    public ErgoValue<Coll<scala.Byte>> getMintingBoxR5() {
        return ErgoValue.of(description.getBytes(StandardCharsets.UTF_8));
    }

    public ErgoValue<Coll<scala.Byte>> getMintingBoxR6() {
        return ErgoValue.of(Integer.toString(decimals).getBytes(StandardCharsets.UTF_8));
    }

    public ErgoValue<?> getMintingBoxR7() {
        return r7;
    }

    public ErgoValue<?> getMintingBoxR8() {
        return r8;
    }

    public ErgoValue<?> getMintingBoxR9() {
        return r9;
    }

    public enum AssetType {
        NONE, NFT_PICTURE, NFT_AUDIO, NFT_VIDEO, MEMBERSHIP_THRESHOLD_SIG, UNKNOWN;

        public byte[] getR7ByteArrayForType() {
            switch (this) {
                case NFT_PICTURE:
                    return new byte[]{1, 1};
                case NFT_AUDIO:
                    return new byte[]{1, 2};
                case NFT_VIDEO:
                    return new byte[]{1, 3};
                case MEMBERSHIP_THRESHOLD_SIG:
                    return new byte[]{2, 1};
                default:
                    return null;
            }
        }
    }
}
