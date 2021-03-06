package edu.mum.product.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import edu.mum.admin.domain.RoleType;
import edu.mum.admin.domain.UserRole;
import edu.mum.customer.domain.User;
import edu.mum.customer.domain.UserProfile;
import edu.mum.product.domain.Catagory;
import edu.mum.product.domain.Order;
import edu.mum.product.domain.OrderLine;
import edu.mum.product.domain.Product;
import edu.mum.product.domain.ProductInventory;
import edu.mum.product.domain.ProductJsonObject;
import edu.mum.product.service.IProductService;
import edu.mum.review.service.IReviewService;

import com.fasterxml.jackson.core.JsonProcessingException;

//@RestController
@Controller
@EnableWebMvc
public class ProductController {

	
	@Autowired
    private IProductService productService;
	
	@Autowired
	private IReviewService reviewService;
	//private Order order = new Order();
	
	
	@RequestMapping("/")
    public String redirectRoot(Model model) {
		
		model.addAttribute("pageTitle", "Home Page");
		model.addAttribute("featuredProducts", productService.getFeaturedProducts());
		model.addAttribute("relatedProducts", productService.getRelatedProducts());
		//System.out.println("\n\n\n\n------------ "+ productService.getFeaturedProducts().size());
        return "home";
    }
	
	@RequestMapping(value = "/home", method = RequestMethod.GET)
    public String showHome(Model model, HttpServletRequest request) {
        
		
		model.addAttribute("pageTitle", "Welcome to Virtual Store");
		List<Product> featuredProducts = productService.getFeaturedProducts();
		
		for( int i = 0 ; i < featuredProducts.size(); i++) {
			ProductInventory pInventory = productService.getProductInventoryByProductId( featuredProducts.get(i).getId() );
			featuredProducts.get(i).setProductInventory(pInventory);
			if( pInventory == null){
				System.out.println( " NO    pInventory Quantity : " );
			}else{
				System.out.println( "pInventory Quantity : "+pInventory.getQuantity() );
			}
				
		}
		model.addAttribute("featuredProducts", featuredProducts);
		model.addAttribute("relatedProducts", productService.getRelatedProducts());
		
		
		
		//System.out.println( featuredProducts.get(0).getProductInventory().getQuantity() );
		

		//System.out.println("\n\n\n\n------------ "+ productService.getFeaturedProducts().size());

		return "home";
    }
	
	@RequestMapping(value="/rest/product/{id}", method=RequestMethod.GET,headers = "Accept=application/json",produces = "application/json")
	public @ResponseBody ProductJsonObject getProduct(@PathVariable("id") int id)
	{
		ProductJsonObject jsonObject=productService.getLatesProduct(id);
		return jsonObject;
	}
	
	@RequestMapping(value = "/productDetails/{productId}", method = RequestMethod.GET)
    public String productDetails(Model model, @PathVariable("productId") Long productId) {
        
		model.addAttribute("pageTitle", "Product Details Page");
		Product product = productService.getProduct( productId);
		ProductInventory productInventory = productService.getProductInventoryByProductId( product.getId());
		product.setProductInventory( productInventory);
		int rating = productService.claculateRatings( product);
		model.addAttribute("product", product);
		model.addAttribute("all_reviews", reviewService.findByProduct(product));
		Double total_rating=reviewService.calculateProductRatings(product);
		model.addAttribute("total_rating", total_rating);
		model.addAttribute("total_rating_width", Math.round( (total_rating/5)*100  ) );
		
		if( rating <= 0){
			model.addAttribute("rating", "N/A");
		}else{
			model.addAttribute("rating", rating);
		}
		
		
		return "productDetails";
    }
	
	
	
	@RequestMapping(value = "/AddToShoppingCart/{productId}", method = RequestMethod.GET)
    public String shoppingCartAdd(Model model, @PathVariable("productId") Long productId, HttpServletRequest request) {
		
		model.addAttribute("pageTitle", "Your Cart");
		
		Order tempOrder = (Order) request.getSession().getAttribute("order");
		if( tempOrder == null){
			tempOrder = new Order();
			//Current user is hard cooded
			User user = new User();
			user.setId(1L);
			
			tempOrder.setUser( user );
			
			OrderLine orderLine = new OrderLine();
			orderLine.setOrder(tempOrder);
			orderLine.setProduct( productService.getProduct(productId));
			orderLine.setQuantity( 1);
			
			tempOrder.getOrderLines().add( orderLine);
			request.getSession().setAttribute("order", tempOrder);
			
		}else{
			boolean productAlreadyInShoppingCar = false;
			for (OrderLine orderLine : tempOrder.getOrderLines()) {
				if( orderLine.getProduct().getId() == productId){
					productAlreadyInShoppingCar = true;
					break;
				}
			}
			
			if(  productAlreadyInShoppingCar ){
				//Do nothing, the product is already in the cart 
			}else{
				OrderLine orderLine = new OrderLine();
				orderLine.setOrder( tempOrder);
				orderLine.setProduct( productService.getProduct(productId));
				orderLine.setQuantity( 1);
				
				tempOrder.getOrderLines().add( orderLine);
				request.getSession().setAttribute("order", tempOrder);
			}
		}
		request.getSession().setAttribute("subtotatl",  productService.claculateSubtotatl(tempOrder) );
		return "shoppingCart";
		//return "redirect:/shoppingCart";
    }
	
	@RequestMapping(value = "/RemoveFromShoppingCart/{productId}", method = RequestMethod.GET)
    public String shoppingCartRemove(Model model, @PathVariable("productId") Long productId, HttpServletRequest request) {
		
		model.addAttribute("pageTitle", "Your Cart");
		
		Order tempOrder = (Order) request.getSession().getAttribute("order");
		if( tempOrder != null){
			//Removing OrderLIne from the product
			int index = 0;
			for (OrderLine orderLine : tempOrder.getOrderLines()) {
				if( orderLine.getProduct().getId() == productId){
					break;
				}
				index++;
			}
			//tempOrder.getOrderLines().remove( productService.getProduct(productId) );
			tempOrder.getOrderLines().remove(index);
			request.getSession().setAttribute("order", tempOrder);
			request.getSession().setAttribute("subtotatl",  productService.claculateSubtotatl(tempOrder) );
		}
		return "shoppingCart";
    }
	
	@RequestMapping(value = "/ChnageQuantityShoppingCart/{productId}", method = RequestMethod.POST)
    public String shoppingCartChangeQuantity(Model model, @PathVariable("productId") Long productId, HttpServletRequest request,
    										@RequestParam("quantity") int quantity) {
		
		model.addAttribute("pageTitle", "Your Cart");
		
		Order tempOrder = (Order) request.getSession().getAttribute("order");
		if( tempOrder != null){
			//Removing OrderLIne from the product
			int index = 0;
			for (OrderLine orderLine : tempOrder.getOrderLines()) {
				if( orderLine.getProduct().getId() == productId){
					break;
				}
				index++;
			}
			//tempOrder.getOrderLines().remove( productService.getProduct(productId) );
			tempOrder.getOrderLines().get(index).setQuantity( quantity);
			request.getSession().setAttribute("order", tempOrder);
			request.getSession().setAttribute("subtotatl",  productService.claculateSubtotatl(tempOrder) );
		}
		System.out.println("CHANGE QUANTITY-------------------------");
		return "redirect:/shoppingCart";
    }
	
	@RequestMapping(value = "/shoppingCart")
    public String shoppingCart(Model model, HttpServletRequest request) {
		
		model.addAttribute("pageTitle", "Your Cart");
		
		//System.out.println("HERERRRRRRRRRRRRRRRRRRRRRRRR");
		return "shoppingCart";
    }
    
	
	@RequestMapping(value = "/checkOut")
    public String checkout(Model model, HttpServletRequest request) {
		
		model.addAttribute("pageTitle", "Check out");
		if( request.getSession().getAttribute("islogged") != null ){
			return "shipping";
		}
		
		
		return "signinORsignup";
    }
	
	@RequestMapping(value = "/shipping")
    public String shippingAndPayment(Model model, HttpServletRequest request) {
		
		model.addAttribute("pageTitle", "Check out");
		
		UserProfile userProfile = (UserProfile)request.getSession().getAttribute("userProfile");
		
		
//		if( request.getSession().getAttribute("islogged") != null ){
//			return "shippingAndPayment";
//		}
//		
		
		return "shipping";
    }
	
	
	@RequestMapping(value = "/search", method = RequestMethod.GET)
    public String search(Model model, @RequestParam("searchedTerm") String searchedTerm, HttpServletRequest request) {
		
		model.addAttribute("pageTitle", "You Searched For ");
		
		List<Product> foundProducts =	productService.searchForProducts( searchedTerm);
		//System.out.println( "\n\n\n\nYou searched for " + searchedTerm + " ---- "+ foundProducts.size());
		model.addAttribute( "foundProducts", foundProducts);
		return "searchPage";
    }
	
	
    /*Avishek*/
    @RequestMapping(value = "/product", method = RequestMethod.GET)
    public String getAll(Model model,HttpServletRequest request) {
    	
    	
		if( request.getSession().getAttribute("islogged") != null && request.getSession().getAttribute("islogged").equals("true")){
			
			UserRole userRole=(UserRole)request.getSession().getAttribute("userRole");
			
			if(userRole.isAdmin())
			{
				model.addAttribute("pageTitle", "Add Product");
				List<Catagory> catagories=productService.getProductCategories();
		    	
		    	model.addAttribute("catagories", catagories);
		    	
				return "product";
			}
			
		}
		
		
		return "redirect:/home";
    	

    }
	
	@RequestMapping(value="/product", method=RequestMethod.POST)
	public String add(Model model, Product product, @RequestParam("catagoryId") int catagoryId, @RequestParam("quantity") int quantity,@RequestParam("file") MultipartFile file,MultipartHttpServletRequest request) {
		
		String fileName=null;

		//http://stackoverflow.com/questions/20162474/how-do-i-receive-a-file-upload-in-spring-mvc-using-both-multipart-form-and-chunk
		
		if (!file.isEmpty()) 
		{
            try 
            {
            	String path=request.getServletContext().getRealPath("/");

                //making directories for our required path.
                byte[] bytes = file.getBytes();
                File directory=    new File(path+ "/uploads");
                if(!directory.exists())
                	{
                		directory.mkdirs();
                	}
                // saving the file
                fileName=System.currentTimeMillis()+".jpg";
                File imageFile=new File(directory.getAbsolutePath()+System.getProperty("file.separator")+fileName);
                BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(imageFile));
                stream.write(bytes);
                stream.close();
                System.out.println("Product Image Saved in "+imageFile.getAbsolutePath());
            } 
            catch (Exception e) 
            {
            	System.out.println("Product Image Saving Failed "+e);
            }
        }
		
		productService.registerProduct(product, catagoryId,quantity,fileName);
		
		model.addAttribute("message", "Product Created ");
		
		return "redirect:/product";
	}
	
	@RequestMapping(value="/product/{id}", method=RequestMethod.GET)
	public String get( Model model, @PathVariable("id") long productId) {
		model.addAttribute("product", productService.getProduct(productId));
		return "productupdate";
	}
	
	@RequestMapping(value="/product/{id}", method=RequestMethod.POST)
	public String update(Model model, Product product,@PathVariable("id") long productId) {
		
		Product updateProduct=productService.getProduct(productId);
		updateProduct.setName(product.getName());
		updateProduct.setPrice(product.getPrice());
		
		productService.modifyProduct(updateProduct); // car.id already set by binding
		
		model.addAttribute("message", "Product Updated");
		
		return "productupdate";
	}
	
	
}
