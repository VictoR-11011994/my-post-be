package com.victorcarablut.code.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.victorcarablut.code.dto.CommentDto;
import com.victorcarablut.code.dto.LikeDto;
import com.victorcarablut.code.entity.post.Comment;
import com.victorcarablut.code.entity.post.Like;
import com.victorcarablut.code.entity.post.Post;
import com.victorcarablut.code.entity.user.User;
import com.victorcarablut.code.exceptions.EmailNotExistsException;
import com.victorcarablut.code.exceptions.ErrorSaveDataToDatabaseException;
import com.victorcarablut.code.exceptions.GenericException;
import com.victorcarablut.code.exceptions.PostMaxLimitException;
import com.victorcarablut.code.repository.CommentRepository;
import com.victorcarablut.code.repository.LikeRepository;
import com.victorcarablut.code.repository.PostRepository;
import com.victorcarablut.code.repository.UserRepository;

@Service
public class PostService {

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private LikeRepository likeRepository;

	@Autowired
	private CommentRepository commentRepository;

	public boolean existsUserByEmail(String email) {
		return userRepository.existsUserByEmail(email);
	}

	public boolean existsPostById(Long id) {
		return postRepository.existsById(id);
	}

	public boolean existsUserById(Long id) {
		return userRepository.existsById(id);
	}

	public List<Post> findAllPosts(Boolean isAdmin, String currentUsernameFromToken, String username) {

		final Long currentUserId = userRepository.findUserIdByUsernameAndReturnOnlyUserId(currentUsernameFromToken);

		List<Post> postsAll = postRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

		ArrayList<Post> postsAllActive = new ArrayList<>();

		for (Post post : postsAll) {

			if (post.getStatus() != null && post.getStatus().equals("active")) {
				postsAllActive.add(post);
			}

			if (likeRepository.existsPostByPostIdAndUserId(post.getId(), currentUserId)) {
				post.setIsCurrentUserLikePost(true);
			} else {
				post.setIsCurrentUserLikePost(false);
			}

			List<Like> likes = likeRepository.findAllByPostId(post.getId());
			post.setTotalLikes(likes.size());
			
			List<Comment> comments = commentRepository.findAllByPostId(post.getId());
			post.setTotalComments(comments.size());

			try {
				postRepository.save(post);
			} catch (Exception e) {
				throw new ErrorSaveDataToDatabaseException();
			}

		}

		if (isAdmin && username == null) {
			// filter on frontend to get only active and non active
			return postsAll;
		} else if (!isAdmin && username == null) {
			return postsAllActive;
		} else {
			return getAllPostsOwner(currentUserId, username);

		}

	}

	public List<Post> getAllPostsOwner(Long currentUserId, String username) {

		final Long userId = userRepository.findUserIdByUsernameAndReturnOnlyUserId(username);

		List<Post> posts = postRepository.findAllByOrderByUserIdDesc(userId);

		for (Post post : posts) {

			if (likeRepository.existsPostByPostIdAndUserId(post.getId(), currentUserId)) {
				post.setIsCurrentUserLikePost(true);
			} else {
				post.setIsCurrentUserLikePost(false);
			}

			List<Like> likes = likeRepository.findAllByPostId(post.getId());
			post.setTotalLikes(likes.size());
			
			List<Comment> comments = commentRepository.findAllByPostId(post.getId());
			post.setTotalComments(comments.size());

			try {
				postRepository.save(post);
			} catch (Exception e) {
				throw new ErrorSaveDataToDatabaseException();
			}

		}

		return posts;
	}

	public void createPost(Post post, MultipartFile image) {

		if (existsUserByEmail(post.getUser().getEmail())) {

			List<Post> posts = postRepository.findAllByOrderByUserIdDesc(post.getUser().getId());

			if (posts.size() >= post.getMaxPostsLimit()) {
				// max limit
				// System.out.println("max limit 3");
				throw new PostMaxLimitException();
			} else {

				// default: "pending", has to be approved by Admin
				post.setStatus("pending");

				post.setCreatedDate(LocalDateTime.now());

				try {
					postRepository.save(post);
				} catch (Exception e) {
					throw new ErrorSaveDataToDatabaseException();
				}

				if (image != null) {
					if (!image.isEmpty()) {

						try {
							uploadImg(post.getUser().getId(), post.getId(), image);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

			}

		} else {
			throw new EmailNotExistsException();
		}

	}

	public void updatePost(Long postId, Post post, MultipartFile image, String imageStatus) {

		Post postUpdate = postRepository.findPostById(postId);

		// System.out.println("image: " + post.getImage());

		postUpdate.setTitle(post.getTitle());
		postUpdate.setDescription(post.getDescription());

		if (imageStatus.contains("no-image")) {
			postUpdate.setImage(null);
			// System.out.println(imageStatus);
		}

		postUpdate.setUpdatedDate(LocalDateTime.now());

		// postUpdate.setStatus("pending");

		try {
			postRepository.save(postUpdate);
		} catch (Exception e) {
			throw new ErrorSaveDataToDatabaseException();
		}

		if (image != null) {
			if (!image.isEmpty()) {

				// post.setImage(image.getBytes());
				try {
					uploadImg(post.getUser().getId(), postId, image);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	public void uploadImg(Long userId, Long postId, MultipartFile file) throws IOException {

		if (existsUserById(userId) && existsPostById(postId)) {

			byte[] decodeUserImgBase64 = null;
			try {
				decodeUserImgBase64 = file.getBytes();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			final long imgSize = decodeUserImgBase64.length;
			Long finalImgSize = 0L;
			String imgSizeFile = "NULL";
			String imgType = file.getContentType().toString();

			long kilobyte = 1024;
			long megabyte = kilobyte * 1024;
			long gigabyte = megabyte * 1024;
			long terabyte = gigabyte * 1024;

			if ((imgSize >= 0) && (imgSize < kilobyte)) {
				finalImgSize = imgSize; // B
				imgSizeFile = "B";

			} else if ((imgSize >= kilobyte) && (imgSize < megabyte)) {
				finalImgSize = (imgSize / kilobyte); // KB
				imgSizeFile = "KB";

			} else if ((imgSize >= megabyte) && (imgSize < gigabyte)) {
				finalImgSize = (imgSize / megabyte); // MB
				imgSizeFile = "MB";

			} else if ((imgSize >= gigabyte) && (imgSize < terabyte)) {
				finalImgSize = (imgSize / gigabyte); // GB
				imgSizeFile = "GB";

			} else if (imgSize >= terabyte) {
				finalImgSize = (imgSize / terabyte); // TB
				imgSizeFile = "TB";

			} else {
				finalImgSize = imgSize;
				imgSizeFile = "NULL";
			}

			// System.out.println(finalImgSize + imgSizeFile + " " + imgType);

			// max: 10 MB
			if ((finalImgSize > 10 && imgSizeFile == "MB") || imgSizeFile == "GB" || imgSizeFile == "TB"
					|| imgSizeFile == "NULL" || !imgType.equals("image/jpeg")) {
				throw new GenericException();
			} else {

				Post post = postRepository.findPostById(postId);
				post.setImage(file.getBytes());
				// post.setUpdatedDate(LocalDateTime.now());

				try {
					postRepository.save(post);
				} catch (Exception e) {
					throw new ErrorSaveDataToDatabaseException();
				}
			}

		} else {
			throw new GenericException();
		}
	}

	public void deletePost(Long userId, Long postId) {

		if (existsUserById(userId) && existsPostById(postId)) {
			postRepository.deleteById(postId);
		} else {
			throw new GenericException();
		}

	}

	// admin only
	public void statusPost(String username, Long userId, Long postId, String status) {

		if (existsUserById(userId) && existsPostById(postId)) {

			Post post = postRepository.findPostById(postId);

			User userAdmin = userRepository.findUserByUsername(username);

			final String userRole = userAdmin.getAuthorities().toString();

			if (userRole.contains("ADMIN")) {

				post.setStatus(status);

				try {
					postRepository.save(post);
				} catch (Exception e) {
					throw new ErrorSaveDataToDatabaseException();
				}

			} else {
				throw new GenericException();
			}

		} else {
			throw new GenericException();
		}
	}

	// ---------- Likes ----------

	public ArrayList<LikeDto> findAllPostLikes(Long postId) {

		List<Like> likes = likeRepository.findAllByPostId(postId);

		ArrayList<LikeDto> likesDto = new ArrayList<>();

		for (Like like : likes) {

			LikeDto likeDto = new LikeDto();
			likeDto.setLikeId(like.getId());
			likeDto.setPostId(like.getPost().getId());
			likeDto.setUserId(like.getUser().getId());
			likeDto.setUserFullName(like.getUser().getFullName());
			likeDto.setUsername(like.getUser().getUsername());
			likeDto.setUserProfileImg(like.getUser().getUserProfileImg());

			likesDto.add(likeDto);

		}
		return likesDto;

	}

	public void postLike(Like like) {

		// only a owner of post can put Like to a post while is on "pending" status,
		// other users only if it is active

		if (!likeRepository.existsPostByPostIdAndUserId(like.getPost().getId(), like.getUser().getId())) {

			likeRepository.save(like);
		} else {
			Like findLike = likeRepository.findByPostIdAndUserId(like.getPost().getId(), like.getUser().getId());

			likeRepository.deleteById(findLike.getId());
		}

	}

	// ---------- Comments ----------

	public ArrayList<CommentDto> findAllPostComments(Long postId) {

		List<Comment> comments = commentRepository.findAllByPostId(postId);

		ArrayList<CommentDto> commentsDto = new ArrayList<>();

		for (Comment comment : comments) {

			CommentDto commentDto = new CommentDto();
			commentDto.setCommentId(comment.getId());
			commentDto.setPostId(comment.getPost().getId());
			commentDto.setUserId(comment.getUser().getId());
			commentDto.setUserFullName(comment.getUser().getFullName());
			commentDto.setUsername(comment.getUser().getUsername());
			commentDto.setUserProfileImg(comment.getUser().getUserProfileImg());
			commentDto.setCreatedDate(comment.getCreatedDate());
			commentDto.setUpdatedDate(comment.getUpdatedDate());

			commentDto.setComment(comment.getComment());

			commentsDto.add(commentDto);

		}
		return commentsDto;

	}

	public void addComment(Comment comment) {

		comment.setCreatedDate(LocalDateTime.now());

		try {
			commentRepository.save(comment);
		} catch (Exception e) {
			throw new ErrorSaveDataToDatabaseException();
		}

	}

	public void updateComment(CommentDto commentDto) {

		if (existsUserById(commentDto.getUserId()) && existsPostById(commentDto.getPostId())) {

			Comment commentUpdate = commentRepository.findCommentById(commentDto.getCommentId());

			commentUpdate.setComment(commentDto.getComment());
			commentUpdate.setUpdatedDate(LocalDateTime.now());

			try {
				commentRepository.save(commentUpdate);
			} catch (Exception e) {
				throw new ErrorSaveDataToDatabaseException();
			}

		} else {
			throw new GenericException();
		}

	}

	public void deleteComment(CommentDto commentDto) {

		if (existsUserById(commentDto.getUserId()) && existsPostById(commentDto.getPostId())) {
			commentRepository.deleteById(commentDto.getCommentId());
		} else {
			throw new GenericException();
		}

	}

}
