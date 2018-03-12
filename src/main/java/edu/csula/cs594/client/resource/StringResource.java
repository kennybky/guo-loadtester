package edu.csula.cs594.client.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/string/")
public class StringResource {

	  	@GET
	    @Path("palindrome")
	    @Produces(MediaType.TEXT_PLAIN)
	    public Response palindrome(@QueryParam("s") String s) {
	  		s = s.toLowerCase();
	  		StringBuilder sb = new StringBuilder(s);
	        String rev = sb.reverse().toString();
	        
	        if (rev.equals(s)) {
	        	return Response.ok().entity("Yes " + s + " is an Palindrome").build();
	        } else
	        	return Response.ok().entity("No " + s + " is not an Palindrome").build();
	        
	    }
	  	
	  	@GET
	  	@Path("insert")
	  	@Produces(MediaType.TEXT_PLAIN)
	  	public Response insert(@QueryParam("s") String s, @QueryParam("t") String t, @QueryParam("u") String u) {
	  		s = s.toLowerCase();
		  	u = u.toLowerCase();
		  	t = t.toLowerCase();
		  	List<Integer> pos = new ArrayList<>();
		  	StringBuilder str = new StringBuilder(s);
		  	for (int i = 0; i < str.length(); i++) {
		  		for (int j = i; j < str.length(); j++) {
		  			if (t.equals(str.substring(i, j))) {
		  				pos.add(j);
		  			}
		  		}
		  	}
		  	for (int m = 1; m < pos.size(); m++) {
		  		Integer r = pos.get(m) + u.length() * m;
		  		pos.set(m, r);
		  	}
		  	for (Integer k : pos) {
		  		str.insert(k, u);
		  	}
		  	return Response.ok(str.toString()).build();
	  	}
	  	
	  	@GET
	  	@Path("datetime")
	  	@Produces(MediaType.TEXT_PLAIN)
	  	public Response dateTime(@QueryParam("format") String s) {
	  		Date date = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat(s);
			String formattedDate = formatter.format(date);
			return Response.ok().entity("The Date is " + formattedDate).build();
	  	}
	  	
	  	@GET
	  	@Path("anagram")
	  	@Produces(MediaType.TEXT_PLAIN)
	  	public Response anagram(@QueryParam("first") String s1, @QueryParam("second") String s2) {
	  	if (s1.length() != s2.length()) {
	  		return Response.ok(s1 + " is not an anagram of " + s2).build();
	  	}
	  	
	  	else{
	  		s1 = s1.toLowerCase();
		  	s2 = s2.toLowerCase();
		  	Map<Character, Integer> mapper = new HashMap<>();
		  	for (int i = 0; i < s1.length(); i ++) {
		  		Character first = s1.charAt(i);
		  		Integer mj = mapper.get(first);
		  		mj = mj == null? 0 : mj;
		  		mapper.put(first, mj+1 );
		  	}
		  	for (int i = 0; i < s2.length(); i ++) {
		  		Character sec = s2.charAt(i);
		  		Integer aj = mapper.get(sec);
		  		aj = aj == null? 0 : aj;
		  		mapper.put(sec, aj-1 );
		  	}
		  	for (Integer v : mapper.values()) {
		  		if (v != 0) {
		  			return Response.ok(s1 + " is not an anagram of " + s2).build();
		  		}
		  	}
		  	return Response.ok(s1 + " is an anagram of " + s2).build();
	  	}
	  	}
	  	
	  	@GET
	  	@Path("occurences")
	  	@Produces(MediaType.APPLICATION_JSON)
	  	public Response occurences(@QueryParam("s") String s) {
	  	
	  		String str = s.toLowerCase();
	  		Map<Character, Integer> mapper = new HashMap<>();
	  		for (int i =0; i < str.length(); i++) { 
	  			Character first = str.charAt(i);
		  		Integer mj = mapper.get(first);
		  		mj = mj == null? 0 : mj;
		  		mapper.put(first, mj+1 );
	  		}
	  		
	  	return Response.ok(mapper).build();
	  	}
	  	
	  	

}
