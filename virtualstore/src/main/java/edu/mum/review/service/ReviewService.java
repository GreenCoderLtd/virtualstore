package edu.mum.review.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.mum.customer.domain.User;
import edu.mum.product.domain.Product;
import edu.mum.review.dao.IReviewDao;
import edu.mum.review.domain.Review;

@Transactional(propagation=Propagation.REQUIRED)
public class ReviewService implements IReviewService {
	
	@Autowired
	private IReviewDao reviewDao;


	public void setReviewDao(IReviewDao reviewDao) {
		this.reviewDao = reviewDao;
	}

	public void addReview(Review review, User user) {
		reviewDao.addReview( review, user);

	}

	@Override
	public List<Review> findByProduct(Product product) {
		// TODO Auto-generated method stub
		return reviewDao.findByProduct(product);
	}

	@Override
	public Double calculateProductRatings(Product product) {
		// TODO Auto-generated method stub
		return reviewDao.calculateProductRatings(product);
	}

}
