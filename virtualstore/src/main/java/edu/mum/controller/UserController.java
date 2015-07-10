package edu.mum.controller;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import edu.mum.dao.UserDAO;
import edu.mum.domain.User;

@Controller
//@RequestMapping(value="/books")
public class UserController {

    @Resource
    private UserDAO userDAO;

    @RequestMapping("/")
    public String redirectRoot(Model model) {
    	
    	
        return "index";
    }
    
    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public String getAll(Model model) {
    	
    	model.addAttribute("user", userDAO.getSampleUser());
    	
    	userDAO.saveUser(userDAO.getSampleUser());
    	
        return "login";
    }


    @ExceptionHandler(value = NoSuchResourceException.class)
    public ModelAndView handle(Exception e) {
        ModelAndView mv = new ModelAndView();
        mv.getModel().put("e", e);
        mv.setViewName("noSuchResource");
        return mv;
    }
}