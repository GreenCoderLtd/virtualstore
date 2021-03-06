package edu.mum.customer.controller;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import edu.mum.admin.domain.PaymentResponse;
import edu.mum.admin.domain.RoleType;
import edu.mum.admin.domain.UserRole;
import edu.mum.admin.domain.Utilities;
import edu.mum.customer.dao.UserDao;
import edu.mum.customer.domain.Address;
import edu.mum.customer.domain.PayType;
import edu.mum.customer.domain.PaymentInfo;
import edu.mum.customer.domain.User;
import edu.mum.customer.domain.UserProfile;
import edu.mum.customer.service.IUserService;
import edu.mum.product.domain.Order;


@Controller
public class CustomerController {
	
	

	@Autowired
    private IUserService userService;
	
	@RequestMapping(value = "/registration", method = RequestMethod.GET)
    public String getAll(Model model) {
    	
        return "registration";
    }
	
	@RequestMapping(value="/registration", method=RequestMethod.POST)
	public String add(Model model, User user, UserProfile userProfile,Address billingAddress,Address shippingAddress) {
		
		userProfile.setBillingAddress(billingAddress);
		userProfile.setShippingAddress(shippingAddress);
		userProfile.setUser(user);
		userService.registerUser(user, userProfile,RoleType.ROLE_USER);
		model.addAttribute("message", "User Registration Successful");
		return "redirect:/login";
	}
	
	
	@RequestMapping(value = "/registrationAtCheckout", method = RequestMethod.GET)
    public String registerAtCheckour(Model model) {
    	
        return "registrationAtCheckout";
    }
	
	@RequestMapping(value="/registrationAtCheckout", method=RequestMethod.POST)
	public String registerAtCheckout(User user, UserProfile userProfile,Address billingAddress,Address shippingAddress) {
		
		userProfile.setBillingAddress(billingAddress);
		userProfile.setShippingAddress(shippingAddress);
		userService.registerUser(user, userProfile,RoleType.ROLE_USER);
		return "redirect:/shippingAndPayment";
	}
	
	@RequestMapping(value="/login", method=RequestMethod.GET)
	public String login( HttpServletRequest request) {
		//If user is already logged in
		if( request.getSession().getAttribute("islogged") != null &&  request.getSession().getAttribute("islogged").equals("true")){
			return "redirect:/home";
		}

		return "/login";
	}
	
	
	@RequestMapping(value="/login", method=RequestMethod.POST)
	public String login( Model model, @RequestParam("username") String username,  
					@RequestParam("password") String password, HttpServletRequest request) {
					
		User user = userService.getUserByUsername( username );
		if(user==null)
		{
			model.addAttribute("message", "Login Failed");
			return "login";
		}
		UserRole userRole = userService.getUserRole(user.getId());
		
		
		if ( user != null && user.getPassword().equals( password ) ) {
			
			UserProfile userProfile = userService.getUserProfileByUserId( user.getId() );	
			userProfile.setUser(user);
			request.getSession().setAttribute("islogged", "true");
			request.getSession().setAttribute("userProfile", userProfile);
			request.getSession().setAttribute("userRole", userRole);
			
		}else{
			model.addAttribute("message", "Login Failed");
			return "login";
		}
		
		//If session is set then change the user
		
		return "redirect:/home";
	}
	
	@RequestMapping(value="/profile", method=RequestMethod.GET)
	public String userProfile( Model model, HttpServletRequest request) {

		if( request.getSession().getAttribute("islogged") != null &&  request.getSession().getAttribute("islogged").equals("true")){
			UserProfile userProfile=(UserProfile) request.getSession().getAttribute("userProfile");
			User user=userProfile.getUser();
			List<Order> allOrders=userService.getAllOrder(user);
			model.addAttribute("allOrders", allOrders);
			return "profile";
		}

		return "/login";
	}
	
	@RequestMapping(value="/logout")
	public String logout( Model model, HttpServletRequest request) {
		
		request.getSession().removeAttribute("islogged");
		request.getSession().invalidate();
		
		return "redirect:/login";
	}
	
	
	@RequestMapping(value = "/payment", method = RequestMethod.GET)
    public String getPayment(Model model) {
    	
        return "addpayment";
    }
	
	
	        
    @RequestMapping(value = "/checkout", method = RequestMethod.GET)
    public String getCheckout(Model model,HttpServletRequest request) {
    	
    	UserProfile userProfile=(UserProfile) request.getSession().getAttribute("userProfile");
		
		model.addAttribute("payments", userProfile.getUser().getPaymentInfos());
    	
        return "checkout";
    }
    
    @RequestMapping(value = "/checkout", method = RequestMethod.POST)
    public String doCheckout(Model model,HttpServletRequest request,@RequestParam("amount") double amount,@RequestParam("paymentId") long paymentId) {
    	
    	UserProfile userProfile=(UserProfile) request.getSession().getAttribute("userProfile");

		PaymentInfo paymentInfo=userService.getPaymentInfo(paymentId);
		
		RestTemplate paymentRest=new RestTemplate();
		
		PaymentResponse paymentResponse = paymentRest.getForObject(Utilities.URL+"/rest/paymentrequest/"+paymentId+"?amount="+amount, PaymentResponse.class);
		
		//PaymentResponse paymentResponse = paymentRest.postForObject(Utilities.URL+"/rest/paymentrequest", paymentInfo, PaymentResponse.class);
		
		model.addAttribute("payments", userProfile.getUser().getPaymentInfos());
		
		model.addAttribute("message", "Payment Status : "+paymentResponse.getPaymentSucess()+" - "+paymentResponse.getMessage());
		
		if(paymentResponse.isCompleted())
		{
			Order order=(Order) request.getSession().getAttribute("order");
			order.setUser(userProfile.getUser());
			
			request.getSession().removeAttribute("order");
			request.getSession().removeAttribute("subtotatl");
			
			userService.recordOrder(order);
			
		}
        return "checkout";
    }
	
	@RequestMapping(value="/payment", method=RequestMethod.POST)
	public String addPayment(Model model, @RequestParam("cardNumber") String cardNumber,@RequestParam("paymentName") String paymentName,@RequestParam("expireDate") String expireDate,HttpServletRequest request) {
		
		UserProfile userProfile=(UserProfile) request.getSession().getAttribute("userProfile");
		
		PaymentInfo paymentInfo=new PaymentInfo();
		paymentInfo.setCardNumber(cardNumber);
		paymentInfo.setPaymentName(paymentName);
		
		Date date=new Date();

		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		try 
		{
			date = formatter.parse(expireDate);
		} 
		catch (ParseException e) 
		{
			
			e.printStackTrace();
		}
		
		paymentInfo.setExpireDate(date);
		
		paymentInfo.setUser(userService.getUser(userProfile.getId()));
		
		paymentInfo.setPaymentAddress(userProfile.getBillingAddress());
		
		paymentInfo.setPayType(PayType.CREDIT_CARD);
		
		userService.addPayment(paymentInfo);
		
		model.addAttribute("message", "Payement Added Sucessfully");
		
		return "addpayment";
	}

	public IUserService getUserService() {
		return userService;
	}
	
	

}
