package textChecker;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/")
public class RestService {

    @GET
    @Path("/status")
    public void status() {}

}
