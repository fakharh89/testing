/******************************************************************************
 *  Copyright (C) Cambridge Silicon Radio Limited 2014
 *
 *  This software is provided to the customer for evaluation
 *  purposes only and, as such early feedback on performance and operation
 *  is anticipated. The software source code is subject to change and
 *  not intended for production. Use of developmental release software is
 *  at the user's own risk. This software is provided "as is," and CSR
 *  cautions users to determine for themselves the suitability of using the
 *  beta release version of this software. CSR makes no warranty or
 *  representation whatsoever of merchantability or fitness of the product
 *  for any particular purpose or use. In no event shall CSR be liable for
 *  any consequential, incidental or special damages whatsoever arising out
 *  of the use of or inability to use this software, even if the user has
 *  advised CSR of the possibility of such damages.
 *
 ******************************************************************************/

package io.blustream.sulley.utilities;


import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import io.blustream.sulley.models.IncorrectImageException;

import static io.blustream.sulley.utilities.ImageEditor.TAG;

class CsConfig {

    private final static short INITIAL_REMAINDER = 0x0000;
    private final static short FINAL_XOR_VALUE = 0x0000;
    private final static short HEADER_LEN = 3;
    private final static short BLOCK_LEN_IN_WORDS = 4;
    private final static short NUM_BLOCKS_POSITION = 1;

    private final static int CSKEY_BLOCK_CRC_POS = 6;
    private final static short IDENTITY_ROOT_LEN_IN_WORDS = 8;
    private final static short ENCRYPTION_ROOT_LEN_IN_WORDS = 8;
    private static final int[] crc_table = {0x0000, 0x8005, 0x800F, 0x000A, 0x801B, 0x001E, 0x0014, 0x8011, 0x8033,
            0x0036, 0x003C, 0x8039, 0x0028, 0x802D, 0x8027, 0x0022, 0x8063, 0x0066, 0x006C, 0x8069, 0x0078, 0x807D,
            0x8077, 0x0072, 0x0050, 0x8055, 0x805F, 0x005A, 0x804B, 0x004E, 0x0044, 0x8041, 0x80C3, 0x00C6, 0x00CC,
            0x80C9, 0x00D8, 0x80DD, 0x80D7, 0x00D2, 0x00F0, 0x80F5, 0x80FF, 0x00FA, 0x80EB, 0x00EE, 0x00E4, 0x80E1,
            0x00A0, 0x80A5, 0x80AF, 0x00AA, 0x80BB, 0x00BE, 0x00B4, 0x80B1, 0x8093, 0x0096, 0x009C, 0x8099, 0x0088,
            0x808D, 0x8087, 0x0082, 0x8183, 0x0186, 0x018C, 0x8189, 0x0198, 0x819D, 0x8197, 0x0192, 0x01B0, 0x81B5,
            0x81BF, 0x01BA, 0x81AB, 0x01AE, 0x01A4, 0x81A1, 0x01E0, 0x81E5, 0x81EF, 0x01EA, 0x81FB, 0x01FE, 0x01F4,
            0x81F1, 0x81D3, 0x01D6, 0x01DC, 0x81D9, 0x01C8, 0x81CD, 0x81C7, 0x01C2, 0x0140, 0x8145, 0x814F, 0x014A,
            0x815B, 0x015E, 0x0154, 0x8151, 0x8173, 0x0176, 0x017C, 0x8179, 0x0168, 0x816D, 0x8167, 0x0162, 0x8123,
            0x0126, 0x012C, 0x8129, 0x0138, 0x813D, 0x8137, 0x0132, 0x0110, 0x8115, 0x811F, 0x011A, 0x810B, 0x010E,
            0x0104, 0x8101, 0x8303, 0x0306, 0x030C, 0x8309, 0x0318, 0x831D, 0x8317, 0x0312, 0x0330, 0x8335, 0x833F,
            0x033A, 0x832B, 0x032E, 0x0324, 0x8321, 0x0360, 0x8365, 0x836F, 0x036A, 0x837B, 0x037E, 0x0374, 0x8371,
            0x8353, 0x0356, 0x035C, 0x8359, 0x0348, 0x834D, 0x8347, 0x0342, 0x03C0, 0x83C5, 0x83CF, 0x03CA, 0x83DB,
            0x03DE, 0x03D4, 0x83D1, 0x83F3, 0x03F6, 0x03FC, 0x83F9, 0x03E8, 0x83ED, 0x83E7, 0x03E2, 0x83A3, 0x03A6,
            0x03AC, 0x83A9, 0x03B8, 0x83BD, 0x83B7, 0x03B2, 0x0390, 0x8395, 0x839F, 0x039A, 0x838B, 0x038E, 0x0384,
            0x8381, 0x0280, 0x8285, 0x828F, 0x028A, 0x829B, 0x029E, 0x0294, 0x8291, 0x82B3, 0x02B6, 0x02BC, 0x82B9,
            0x02A8, 0x82AD, 0x82A7, 0x02A2, 0x82E3, 0x02E6, 0x02EC, 0x82E9, 0x02F8, 0x82FD, 0x82F7, 0x02F2, 0x02D0,
            0x82D5, 0x82DF, 0x02DA, 0x82CB, 0x02CE, 0x02C4, 0x82C1, 0x8243, 0x0246, 0x024C, 0x8249, 0x0258, 0x825D,
            0x8257, 0x0252, 0x0270, 0x8275, 0x827F, 0x027A, 0x826B, 0x026E, 0x0264, 0x8261, 0x0220, 0x8225, 0x822F,
            0x022A, 0x823B, 0x023E, 0x0234, 0x8231, 0x8213, 0x0216, 0x021C, 0x8219, 0x0208, 0x820D, 0x8207, 0x0202};
    private static final String DEFAULT_ROOTS = "00000000000000000000000000000000";
    private static int mNumBlocks = 0;
    private static int mCsBlkOffset;
    private static int mKeyBlockLenInWords;
    private static short mCrcRemainder;

    /**
     * Reflection for compatibility between the sender and receiver of a transmission.
     *
     * @param original byte original data.
     * @return short result of reflection
     */
    private static short reflect(byte original) {
        short reflection;
        short reflectedBit;
        byte bitCount;

        /* Generate the initial reflection and reflected bit */
        reflection = 0;
        reflectedBit = (short) (((1 << ((byte) 8 - 1)) & 0xff) & 0xffff);

        /* Reflect the data about the center bit. */
        for (bitCount = 0; bitCount < (byte) 8; bitCount++) {
            /* If the LSB is set, set the reflection of it */
            if ((short) (original & 0x01) == 0x01) {
                reflection |= reflectedBit;
            }

            /* Next bit */
            original >>>= 1;
            reflectedBit >>>= 1;
        }

        return reflection;
    }

    /**
     * Reset an an initial value with which CRC computation would start
     */
    private static void crcReset() {
        mCrcRemainder = INITIAL_REMAINDER;
    }

    /**
     * CRC computation for one byte
     *
     * @param one byte data
     */
    private static void crcAddByte(byte one) {
        short byte1 = (short) (reflect(one) & 0xff);
        short byte2 = (short) (((mCrcRemainder >>> 8)) & 0xff);
        short data = (short) ((byte1 ^ byte2) & 0xff);
        mCrcRemainder = (short) (crc_table[data] ^ (short) (mCrcRemainder << 8));
    }

    /**
     * Read CRC
     *
     * @return short result
     */
    private static short crcRead() {
        return (short) ((mCrcRemainder ^ FINAL_XOR_VALUE) & 0xffff);
    }

    /**
     * updates the device image file with the supplied BT address and device crystal trim, updating the header block
     * CRCs on the way.
     *
     * @param bb     byte[] original image data.
     * @param bdaddr String Bluetooth address
     * @return byte[] result
     */
    static byte[] mergeKeys(byte[] bb, String bdaddr) throws IncorrectImageException, StringIndexOutOfBoundsException {
        String content = new String(bb, StandardCharsets.US_ASCII);
        int BtAddrLineOffset = -1;
        int iRootLineOffset = 0xffff;
        int iEncryptionRootOffset = 0xffff;
        int mLinesCount = 0;

        String[] lines = content.split("\\r\\n");

        int linecount = 0;

        int indexofBtAddress = 0;
        int indexOfIdentityRoot = 0;
        int indexOfEncryptionRoot = 0;
        boolean isNumBlocksValid = false;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains("/"))
                continue;

            if (linecount == NUM_BLOCKS_POSITION) {
                Log.d(TAG, "NUM_BLOCKS_POSITION");
                int idx = lines[i].lastIndexOf(" ");
                String subString = lines[i].substring(idx + 1, idx + 3);
                mNumBlocks = Integer.parseInt(subString, 16);
                BtAddrLineOffset = HEADER_LEN + (mNumBlocks * BLOCK_LEN_IN_WORDS) + 1;
                mCsBlkOffset = HEADER_LEN + (mNumBlocks * 4);
                isNumBlocksValid = true;
                iRootLineOffset = HEADER_LEN + (mNumBlocks * BLOCK_LEN_IN_WORDS) + 30;
                iEncryptionRootOffset = iRootLineOffset + IDENTITY_ROOT_LEN_IN_WORDS;
            } else if (linecount == HEADER_LEN + 2) {
                int idx = lines[i].lastIndexOf(" ");
                String subString = lines[i].substring(idx + 1);
                mKeyBlockLenInWords = Integer.parseInt(subString, 16) / 2;
            }
//            // BT Address
            else if (isNumBlocksValid && (linecount >= BtAddrLineOffset && linecount < BtAddrLineOffset + 3)) {
                int startIdx = lines[i].lastIndexOf(" ");
                String indexValue = lines[i].substring(0, startIdx + 1);
                int btIdx = bdaddr.indexOf(":");
                if (btIdx >= 0)
                    bdaddr = bdaddr.replace(":", "");
                String newAddress =
                        bdaddr.substring(bdaddr.length() - indexofBtAddress * 4 - 4, bdaddr.length() - indexofBtAddress * 4);
                lines[i] = indexValue + newAddress;
                indexofBtAddress++;
            } else if (isNumBlocksValid && linecount >= iRootLineOffset && linecount < iRootLineOffset + IDENTITY_ROOT_LEN_IN_WORDS) {
                int idx = lines[i].lastIndexOf(" ");
                String indexValue = lines[i].substring(0, idx + 1);
                String lsbValue = DEFAULT_ROOTS.substring(indexOfIdentityRoot * 4, indexOfIdentityRoot * 4 + 2);
                String msbValue = DEFAULT_ROOTS.substring(indexOfIdentityRoot * 4 + 2, indexOfIdentityRoot * 4 + 4);
                lines[i] = indexValue + msbValue + lsbValue;
                indexOfIdentityRoot++;
            } else if (isNumBlocksValid && linecount >= iEncryptionRootOffset && linecount < iEncryptionRootOffset + ENCRYPTION_ROOT_LEN_IN_WORDS) {
                int idx = lines[i].lastIndexOf(" ");
                String indexValue = lines[i].substring(0, idx + 1);
                String lsbValue = DEFAULT_ROOTS.substring(indexOfEncryptionRoot * 4, indexOfEncryptionRoot * 4 + 2);
                String msbValue = DEFAULT_ROOTS.substring(indexOfEncryptionRoot * 4 + 2, indexOfEncryptionRoot * 4 + 4);
                lines[i] = indexValue + msbValue + lsbValue;
                indexOfEncryptionRoot++;
            }
            int idx = lines[i].lastIndexOf("@");
            if (idx >= 0)
                linecount++;
        }

        mLinesCount = linecount;

        short CsBlockCrc = calculateCsBlkHdCrc(lines);
        updateCsBlockCrc(lines, CsBlockCrc);
        short ControlHdrCrc = calculateControlHdCrc(lines);
        updateControlHdrCrc(lines, ControlHdrCrc);

        ByteBuffer bbf = ByteBuffer.allocate(mLinesCount * 2);
        for (String line : lines) {
            if (line.contains("//"))
                continue;

            int idx = line.lastIndexOf("@");
            if (idx < 0)
                continue;

            idx = line.lastIndexOf(" ");
            if (idx > 0) {
                String subString = line.substring(idx + 1);
                if (subString.length() != 4) {
                    throw new IncorrectImageException("Incorrect image file...");
                }
                int code = Integer.valueOf(subString, 16);
                byte one = (byte) (code & 0xff);
                byte two = (byte) ((code >>> 8) & 0xff);
                bbf.put(one);
                bbf.put(two);
            }

        }
        return bbf.array();
    }

    /**
     * Calculate Block Headers CRC
     *
     * @param lines String[] ASCII data
     * @return byte[] CRC result
     */
    private static short calculateCsBlkHdCrc(String[] lines) {

        int linecount = 0;

        crcReset();

        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains("//"))
                continue;

            if (linecount < mCsBlkOffset) {
                linecount++;
                continue;
            }

            if (linecount < mCsBlkOffset + mKeyBlockLenInWords) {

                int idx = lines[i].lastIndexOf(" ");
                if (idx >= 0) {
                    String subString = lines[i].substring(idx + 1);
                    try {
                        int value = Integer.parseInt(subString, 16);
                        crcAddByte((byte) (value & 0xff));
                        crcAddByte((byte) ((value >>> 8) & 0xff));
                    } catch (NumberFormatException e) {
                        Log.w("CSConfig", "calculateCsBlkHdCrc exception at image file line[" + i + "]=" + subString);
                    }

                }
            } else
                break;

            linecount++;
        }
        return crcRead();
    }

    /**
     * Update Block Headers CRC in buffer
     *
     * @param lines      String[] ASCII data
     * @param csBlockCrc short updated block header CRC
     */
    private static void updateCsBlockCrc(String[] lines, short csBlockCrc) {
        int linecount = 0;

        for (int i = 0; i < lines.length; i++) {
            int idx = lines[i].indexOf("//");
            if (idx >= 0)
                continue;

            if (linecount == CSKEY_BLOCK_CRC_POS) {
                idx = lines[i].lastIndexOf(" ");
                String indexValue = lines[i].substring(0, idx + 1);
                String newCrcValue = String.format("%04x", csBlockCrc);
                lines[i] = indexValue + newCrcValue;
                break;
            }
            linecount++;
        }
    }

    /**
     * Calculate Control Headers CRC
     *
     * @param lines String[] ASCII data
     * @return byte[] CRC result
     */
    private static short calculateControlHdCrc(String[] lines) {
        int HeadersLen = HEADER_LEN + (mNumBlocks * 4);
        crcReset();
        int linecount = 0;
        for (String line : lines) {
            int idx = line.indexOf("//");
            if (idx >= 0)
                continue;

            if (linecount >= HeadersLen)
                break;

            if (linecount > 0) {
                idx = line.lastIndexOf(" ");
                String subString = line.substring(idx + 1);
                int value = Integer.parseInt(subString, 16);
                crcAddByte((byte) (value & 0xff));
                crcAddByte((byte) ((value >>> 8) & 0xff));
            }

            linecount++;
        }

        return crcRead();
    }

    /**
     * Update Control Headers CRC in buffer
     *
     * @param lines         String[] ASCII data
     * @param ControlHdrCrc short updated control header CRC
     */
    private static void updateControlHdrCrc(String[] lines, short ControlHdrCrc) {
        int linecount = 0;

        for (int i = 0; i < lines.length; i++) {
            int idx = lines[i].indexOf("//");
            if (idx < 0) {
                linecount = i;
                break;
            }
        }

        int idx = lines[linecount].lastIndexOf(" ");
        String indexValue = lines[linecount].substring(0, idx + 1);

        String newCrcValue = String.format("%04x", ControlHdrCrc);
        lines[linecount] = indexValue + newCrcValue;
    }

}
