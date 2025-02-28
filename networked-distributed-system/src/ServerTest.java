import static org.junit.Assert.*;
import org.junit.Test;
import java.io.*;
import java.net.*;

public class ServerTest {

    @Test
    public void testServerSingleton() {
        Server server1 = Server.getInstance();
        Server server2 = Server.getInstance();
        assertSame(server1, server2);
    }

    @Test
    public void testClientConnection() {
        new Thread(() -> Server.main(new String[]{})).start();

        try {
            Socket clientSocket1 = new Socket("localhost", 12345);
            PrintWriter out1 = new PrintWriter(clientSocket1.getOutputStream(), true);
            BufferedReader in1 = new BufferedReader(new InputStreamReader(clientSocket1.getInputStream()));
            out1.println("1");
            String response1 = in1.readLine();
            assertNotNull(response1);

            Socket clientSocket2 = new Socket("localhost", 12345);
            PrintWriter out2 = new PrintWriter(clientSocket2.getOutputStream(), true);
            BufferedReader in2 = new BufferedReader(new InputStreamReader(clientSocket2.getInputStream()));
            out2.println("2");
            String response2 = in2.readLine();
            assertNotNull(response2);

            clientSocket1.close();
            clientSocket2.close();
        } catch (IOException e) {
            fail("Client connection test failed: " + e.getMessage());
        }
    }
}