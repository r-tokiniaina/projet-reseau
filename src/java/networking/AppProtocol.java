package networking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AppProtocol {
    public static void writeTo(InputStream in, OutputStream out, long totalSize) throws IOException {
        byte[] buffer = new byte[8192];
        int bytesRead;
        long totalBytesRead = 0;

        while (totalBytesRead < totalSize) {
            int toRead = (int) Math.min(buffer.length, totalSize - totalBytesRead);
            bytesRead = in.read(buffer, 0, toRead);
            if (bytesRead == -1) {
                break;
            }
            out.write(buffer, 0, bytesRead);
            totalBytesRead += bytesRead;
        }
    }

    public static void writeTo(InputStream in, OutputStream out) throws IOException {
        in.transferTo(out);
    }
}
