<%@page import="java.lang.*"%>
<%
if( request.getSession().getAttribute("islogged") != null && request.getSession().getAttribute("islogged").equals("true")){
	//out.println( "YOU ARE LOGGED IN - checking.jsp" );	
}else{
	//user is not logged in
	String site = new String("http://localhost:8080/virtualstore/login");
	response.sendRedirect( site);
	
}
	
%>

<!-- // 	}
<!-- // if( request.getSession().getAttribute("username") != null){ -->
	
	
<!-- // 	if( request.getSession().getAttribute("username").equals("powner") ){ -->
		
<!-- // 		String site = new String("http://localhost:8080/MumScrum/productOwner"); -->
<!-- // 		response.sendRedirect( site); -->
<!-- // 	}else if( request.getSession().getAttribute("username").equals("tester")){ -->
	
		
	
<!-- // 	}else if( request.getSession().getAttribute("username").equals("developer")){ -->
		
	
<!-- // 	}else if( request.getSession().getAttribute("username").equals("smaster")){ -->
		
<!-- // 	} -->
<!-- // }else{ -->

<!-- // 	String username = (String)request.getParameter("username"); -->
<!-- // 	if( username != null){ -->
<!-- // 		request.getSession().setAttribute("username", (String)request.getParameter("username")); -->
<!-- // 		String site = new String("http://localhost:8080/MumScrum/login"); -->
<!-- // 		response.setHeader("Location", site); 	 -->
<!-- // 	} -->
<!-- // } -->
