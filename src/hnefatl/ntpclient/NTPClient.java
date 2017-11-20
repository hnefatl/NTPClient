package hnefatl.ntpclient;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteOrder;
import java.time.Instant;

// Using the description given here:
// https://www.cisco.com/c/en/us/about/press/internet-protocol-journal/back-issues/table-contents-58/154-ntp.html

public class NTPClient
{
    public static void main(String[] args)
    {
        try
        {
            String ip = args[0];
            int port = Integer.parseInt(args[1]);

            try (DatagramSocket sock = new DatagramSocket())
            {
                byte[] headerBytes = new byte[4 * 4];

                // Leap Indicator:  3 = Clock is unsychronised (do we want a leap second)
                // Version Number:  3
                // Mode:            3 = Client
                headerBytes[0] = 0b11_011_011;

                // Stratum:         0 = Unspecified (what level is the server at?)
                headerBytes[1] = 0;

                // Poll:            4 (log2 value of the max seconds between polls. 4 => 16 seconds)
                headerBytes[2] = (byte)6;

                // Precision:      -4 (precision of the system clock in log2 seconds. -4 => 1/16th of a second)
                headerBytes[3] = (byte)-4;

                // Root Delay (server only)
                for (int x = 4; x < 8; ++x)
                    headerBytes[x] = 0;
                // Root Dispersion (server only)
                for (int x = 8; x < 12; ++x)
                    headerBytes[x] = 0;
                // Reference identifier (server only)
                for (int x = 12; x < 16; ++x)
                    headerBytes[x] = 0;

                
            }
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            System.out.println("Usage: ntpclient <ntp server> <port>");
        }
        catch (NumberFormatException e)
        {
            System.out.println("Invalid port");
        }
    }

    private static void setTime(byte[] buffer, int index)
    {
        Instant now = Instant.now();

        // 2208988800L is the seconds from 1900 to 1970, converting from java 1970 epoch to ntp 1900 epoch
        int seconds = now.getEpochSecond() + 2208988800L;
        // Fractional seconds (% 1000000000 caps the nanoseconds to be fractional within a second)
        int fractional = now.getNano() % 1000000000;

        setTime(buffer, index, seconds);
        setTime(buffer, index + 4, fractional);
    }
    private static void setTime(byte[] buffer, int index, int value)
    {
        if (ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN))
        {
            for (int x = 0; x < 4; ++x)
                buffer[x] = value & (0xFF << (32 - 8 * x));
        }
        else
        {
            for (int x = 0; x < 4; ++x)
                buffer[x] = value & (0xFF << (8 * x));
        }
    }
}