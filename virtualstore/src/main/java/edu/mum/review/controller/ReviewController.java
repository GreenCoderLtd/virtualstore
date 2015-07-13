package edu.mum.review.controller;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import edu.mum.customer.domain.User;
import edu.mum.customer.service.IUserService;
import edu.mum.product.domain.Product;
import edu.mum.product.service.IProductService;
import edu.mum.review.domain.Review;
import edu.mum.review.service.IReviewService;

@Controller
public class ReviewController {
	
	@Autowired
	private IProductService productService;
	
	@Autowired
	private IReviewService reviewService;
	
	@Autowired
	private IUserService userService;
	
	@RequestMapping(value="/productReview/{pid}", method = RequestMethod.GET)
	public String getAll(@PathVariable("pid") Long pid, Model model) 
	{
		Product product = productService.getProduct(pid);
		model.addAttribute("product",product);

		return "review";
	  }
	
	@RequestMapping(value="/productR", method=RequestMethod.POST)
	public String addReview(@RequestParam("rating") int rating ,@RequestParam("comment") String comment,@RequestParam("product") long pid, Model model) 
	{
		
		Product product = productService.getProduct((long) pid);
		
		System.out.println( "\n\n\nJUST--------------- 2\n\n\n");
		//User is hard cooded
		User user = userService.getUser(1L);
		
		System.out.println( "\n\n\nJUST--------------- 3\n\n\n");
		if( user == null)
			System.out.println( "You are null man");
		
		Review review = new Review();
		review.setComment(comment);
		review.setRating(rating);
		//review.setUser(user);
		review.setProduct(product);
		review.setDate( new Date());
		reviewService.addReview(review, user);
		return "redirect:/productDetails/"+pid;
	}


}