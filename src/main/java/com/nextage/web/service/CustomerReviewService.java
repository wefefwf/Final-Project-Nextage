package com.nextage.web.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.nextage.web.domain.BizinfoDTO;
import com.nextage.web.domain.CareerDTO;
import com.nextage.web.domain.KitDTO;
import com.nextage.web.domain.ReviewDTO;
import com.nextage.web.mapper.BusinessPortfolioMapper;
import com.nextage.web.mapper.CustomerReviewMapper;
import com.nextage.web.mapper.CustomerShopMapper;

@Service
public class CustomerReviewService {

	@Autowired
	public CustomerReviewMapper  cMapper;
	
	//저장 path
	@Value("${file.upload-dir}")
    private String uploadDir;
	
	//이름 바꾸고 확장자바꾸는용도
		public String uploadFile(MultipartFile file) {
			
		    // 1. 파일이 비어있으면(안 올렸으면) 그냥 null 돌려주기
			if (file == null || file.isEmpty()) return null;
			
			String originalFilename = file.getOriginalFilename();
		    String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
		    String uuidName = UUID.randomUUID().toString() + extension;
		    
		 // 1. DB에 저장될 경로 (통합된 /images/kit/ 경로 사용)
		    String dbPath = "/images/" + uuidName;
		    

		 // 2. 물리적 저장 경로 (D:/nextageImage/kit/uuid.jpg)
		    try {
		        // uploadDir(D:/nextageImage) 바로 아래의 kit 폴더에 저장
		    	String savePath = uploadDir.endsWith("/") ? uploadDir : uploadDir + "/";
		        
		        File folder = new File(savePath);
		        if (!folder.exists()) folder.mkdirs(); 

		        file.transferTo(new File(savePath + uuidName));
		    } catch (IOException e) {
		        e.printStackTrace();
		        return null;
		    }

		    return dbPath;
		}
		
		//review들고오기
		public ReviewDTO getOrderItemForReview(long orderItemId,String loginId){
			return cMapper.getOrderItemForReview(orderItemId,loginId);
		}
}
