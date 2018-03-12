package edu.csula.cs594.client.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;

@Path("/math/")
public class MathResource {

    //http://www.sanfoundry.com/java-program-find-gcd-lcm-two-numbers/
    @GET
    @Path("gcd")
    @Produces(MediaType.TEXT_PLAIN)
    public int gcd(@QueryParam("x") int x, @QueryParam("y") int y) {
        int r = 0, a, b;
        a = (x > y) ? x : y; // a is greater number
        b = (x < y) ? x : y; // b is smaller number
        r = b;
        while (a % b != 0) {
            r = a % b;
            a = b;
            b = r;
        }
        return r;
    }

    //http://www.sanfoundry.com/java-program-find-gcd-lcm-two-numbers/
    @GET
    @Path("lcm")
    @Produces(MediaType.TEXT_PLAIN)
    public int lcm(@QueryParam("x") int x, @QueryParam("y") int y) {
        int a;
        a = (x > y) ? x : y; // a is greater number
        while (true) {
            if (a % x == 0 && a % y == 0)
                return a;
            ++a;
        }
    }

    @GET
    @Path("nfibonacci")
    @Produces(MediaType.TEXT_PLAIN)
    public int nthFib(@QueryParam("n") int n) {
        switch (n) {
            case 0:
                return 0;
            case 1:
                return 1;
            default:
                return nthFib(n - 1) + nthFib(n - 2);
        }
    }

    @GET
    @Path("fibnumbers")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Integer> fibNumbers(@QueryParam("n") int n) {
        Integer[] fibNums = new Integer[n];
        fibNums[0] = 0;
        fibNums[1] = 1;

        for (int i = 2; i < n; i++) {
            fibNums[i] = fibNums[i - 1] + fibNums[i - 2];
        }
        return Arrays.asList(fibNums);
    }

    //https://gist.github.com/mdp/9691528
    @GET
    @Path("validcc")
    @Produces(MediaType.TEXT_PLAIN)
    public Response validCC(@QueryParam("CreditCardStr") String ccNumber) {
        if (null == ccNumber || ccNumber.isEmpty()) {
            return Response.ok().entity("Invalid or empty parameter for CreditCardStr").build();
        } else {
            int sum = 0;
            boolean alternate = false;
            for (int i = ccNumber.length() - 1; i >= 0; i--) {
                int n = Integer.parseInt(ccNumber.substring(i, i + 1));
                if (alternate) {
                    n *= 2;
                    if (n > 9) {
                        n = (n % 10) + 1;
                    }
                }
                sum += n;
                alternate = !alternate;
            }
            return Response.ok().entity(sum % 10 == 0).build();
        }
    }
}
