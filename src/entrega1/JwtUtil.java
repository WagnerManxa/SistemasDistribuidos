package entrega1;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JwtUtil {
    private static final String SECRET_KEY = "AoT3QFTTEkj16rCby/TPVBWvfSQHL3GeEz3zVwEd6LDrQDT97sgDY8HJyxgnH79jupBWFOQ1+7fRPBLZfpuA2lwwHqTgk+NJcWQnDpHn31CVm63Or5c5gb4H7/eSIdd+7hf3v+0a5qVsnyxkHbcxXquqk9ezxrUe93cFppxH4/kF/kGBBamm3kuUVbdBUY39c4U3NRkzSO+XdGs69ssK5SPzshn01axCJoNXqqj+ytebuMwF8oI9+ZDqj/XsQ1CLnChbsL+HCl68ioTeoYU9PLrO4on+rNHGPI0Cx6HrVse7M3WQBPGzOd1TvRh9eWJrvQrP/hm6kOR7KrWKuyJzrQh7OoDxrweXFH8toXeQRD8";

    public static String generateToken(String userId, Boolean isAdmin) {
        return Jwts.builder()
                .setSubject(userId)
                .claim("admin", isAdmin)
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

   public static Claims decodeToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }
   
   public static boolean isUserAdmin(String token) {
	    Claims claims = decodeToken(token);

	    if (claims != null) {
	        return claims.get("admin", Boolean.class);
	    }

	    return false;
	}
   
   public static String getUserIdFromToken(String token) {
	    Claims claims = decodeToken(token);

	    if (claims != null) {
	        return claims.getSubject();
	    }

	    return null; 
	}
   public static Boolean isValidToken(String token) {
       Claims claims = decodeToken(token);

       if (claims != null) {
           String tokenUserId = claims.getSubject();
           Boolean tokenIsAdmin = claims.get("admin", Boolean.class);
           if ((tokenUserId == null) || (tokenIsAdmin == null)) {
        	   return false;
           }else
        	   return true;
       }

       return false;
   }
}
