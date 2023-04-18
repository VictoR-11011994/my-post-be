package com.victorcarablut.code.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.util.ArrayUtils;

import com.victorcarablut.code.dto.LikeDto;
import com.victorcarablut.code.entity.post.Like;
import com.victorcarablut.code.entity.post.Post;
import com.victorcarablut.code.entity.user.User;
import com.victorcarablut.code.exceptions.EmailNotExistsException;
import com.victorcarablut.code.exceptions.ErrorSaveDataToDatabaseException;
import com.victorcarablut.code.exceptions.GenericException;
import com.victorcarablut.code.exceptions.InvalidEmailException;
import com.victorcarablut.code.exceptions.PostMaxLimitException;
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

	public boolean existsUserByEmail(String email) {
		return userRepository.existsUserByEmail(email);
	}

	public boolean existsPostById(Long id) {
		return postRepository.existsById(id);
	}

	public boolean existsUserById(Long id) {
		return userRepository.existsById(id);
	}


	public List<Post> findAllPosts(Boolean isAdmin) {

		List<Post> postsAll = postRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

		ArrayList<Post> postsAllActive = new ArrayList<>();

		for (Post post : postsAll) {

			if (post.getIsActive() != null && post.getIsActive()) {
				postsAllActive.add(post);

				// System.out.println(post.getIsActive());

				List<Like> likes = likeRepository.findAllByPostId(post.getId());

				ArrayList<LikeDto> likesDto = new ArrayList<>();

				for (Like like : likes) {

					LikeDto likeDto = new LikeDto();
					likeDto.setLikeId(like.getId());
					likeDto.setPostId(like.getPost().getId());
					likeDto.setUserId(like.getUser().getId());
					likeDto.setUserFullName(like.getUser().getFullName());
					likeDto.setUsername(like.getUser().getUsername());

					likesDto.add(likeDto);
				}

				post.setLikes(likesDto);

			}

		}


		
		if(isAdmin) {
			return postsAll;
		} else {
			return postsAllActive;
		}

		
	}

	public List<Post> findAllPostsOwner(String username) {

		User user = userRepository.findUserByUsername(username);

		List<Post> posts = postRepository.findAllByOrderByUserIdDesc(user.getId());

		for (Post post : posts) {

			List<Like> likes = likeRepository.findAllByPostId(post.getId());

			ArrayList<LikeDto> likesDto = new ArrayList<>();

			for (Like like : likes) {

				LikeDto likeDto = new LikeDto();
				likeDto.setLikeId(like.getId());
				likeDto.setPostId(like.getPost().getId());
				likeDto.setUserId(like.getUser().getId());
				likeDto.setUserFullName(like.getUser().getFullName());
				likeDto.setUsername(like.getUser().getUsername());

				likesDto.add(likeDto);

			}

			post.setLikes(likesDto);
		}

		return posts;
	}

	public void createPost(Post post, MultipartFile image) {

		if (existsUserByEmail(post.getUser().getEmail())) {

			List<Post> posts = postRepository.findAllByOrderByUserIdDesc(post.getUser().getId());

			// System.out.println(posts.size());

			if (posts.size() >= post.getMaxPostsLimit()) {
				// max limit
				// System.out.println("max limit 3");
				throw new PostMaxLimitException();
			} else {

				// default: false, has to be approved by Admin
				post.setIsActive(false);

				post.setCreatedDate(LocalDateTime.now());

				try {
					postRepository.save(post);
				} catch (Exception e) {
					throw new ErrorSaveDataToDatabaseException();
				}

				if (image != null) {
					if (!image.isEmpty()) {

						// post.setImage(image.getBytes());
						try {
							uploadImg(post.getUser().getId(), post.getId(), image);
						} catch (IOException e) {
							// TODO Auto-generated catch block
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

		// System.out.println("userId: " + post.getUser().getId());
		// System.out.println("postId: " + postId);

		// System.out.println("title: " + post.getTitle());
		// System.out.println("desc: " + post.getDescription());
		System.out.println("image: " + post.getImage());

		postUpdate.setTitle(post.getTitle());
		postUpdate.setDescription(post.getDescription());

		if (imageStatus.contains("no-image")) {
			postUpdate.setImage(null);
			System.out.println(imageStatus);
		}

		postUpdate.setUpdatedDate(LocalDateTime.now());

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

			System.out.println(finalImgSize + imgSizeFile + " " + imgType);

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

	// ---------- Likes ----------

	public void userLike(Like like) {

		if (!likeRepository.existsPostByPostIdAndUserId(like.getPost().getId(), like.getUser().getId())) {

			likeRepository.save(like);
		} else {
			Like findLike = likeRepository.findByPostIdAndUserId(like.getPost().getId(), like.getUser().getId());

			likeRepository.deleteById(findLike.getId());
		}

	}

}
