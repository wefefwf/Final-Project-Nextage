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
import com.nextage.web.mapper.CustomerShopMapper;

@Service
public class BusinessPortfolioService {

	@Autowired
	public BusinessPortfolioMapper portfolioMapper;
	
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
		
	//이미지 수정
	public void updateProfile(long businessId,MultipartFile file){
		
		//이름 저장후 불러내기
		String profileImage = uploadFile(file);
		
		if (profileImage != null) {
			portfolioMapper.updateProfile(businessId, profileImage);
	    }
	}
		
	//업체정보 
	public BizinfoDTO getPortfolio(long id){
		return portfolioMapper.getPortfolio(id);
	}
	
	//리뷰 들고오기
	public List<ReviewDTO> getReview(long id,int size,int offset,boolean isMine){
		return portfolioMapper.getReview(id,size,offset,isMine);
	}
	
	//경력 들고오기
	public List<CareerDTO> getCareer(long id){
		return portfolioMapper.getCareer(id);
	}
	
	//리뷰 갯수들고오기 
		public long getTotalReviewCount(long id,boolean isMine){
			return portfolioMapper.getTotalReviewCount(id,isMine);
		}
		
	//status 변경
		public void updateStatus(long reviewId, String status){
			portfolioMapper.updateStatus(reviewId, status);
		}
	//Location 업데이트
		public void updateLocation(long businessId,String location){
			portfolioMapper.updateLocation(businessId,location);
		}
	
	//Location삭제
		public void deleteLocation(long businessId) {
	        // 컨트롤러에서 null을 보내면 DB 컬럼이 NULL로 업데이트됨
	        portfolioMapper.deleteLocation(businessId);
	    }
		//이미지 삭제
	public void deleteProfileImage(long businessId) {
        // 컨트롤러에서 null을 보내면 DB 컬럼이 NULL로 업데이트됨
        portfolioMapper.deleteProfileImage(businessId);
    }
	
	// 경력 삭제 (이건 행 자체를 날리는 거니까 id 하나만)
    public void deleteCareer(long careerId) {
        portfolioMapper.deleteCareer(careerId);
    }
    
    //경력 수정
    public void updateCareer(long careerId,String workDescription){
    	portfolioMapper.updateCareer(careerId,workDescription);
    }
    
    //경력 추가
    public void addCareer(long businessId,String workDescription){
    	portfolioMapper.addCareer(businessId,workDescription);
    }
}
