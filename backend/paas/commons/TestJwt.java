import com.houjiang.commons.util.JwtUtil;

public class TestJwt {
    public static void localMain(String[] args) {
        try {
            String userId = "640697115455213568";
            String token = JwtUtil.createJWT(userId);
            System.out.println("Generated JWT: " + token);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}