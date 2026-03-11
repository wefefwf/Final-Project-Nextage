package com.nextage.web.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.nextage.web.domain.KitDTO;
import com.nextage.web.mapper.CustomerShopMapper;

@Service
public class CustomerShopService {

	@Autowired
	public CustomerShopMapper shopMapper;
	
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
	    String dbPath = "/images/kit/" + uuidName;
	    

	 // 2. 물리적 저장 경로 (D:/nextageImage/kit/uuid.jpg)
	    try {
	        // uploadDir(D:/nextageImage) 바로 아래의 kit 폴더에 저장
	        String savePath = uploadDir.endsWith("/") ? uploadDir + "kit/" : uploadDir + "/kit/";
	        
	        File folder = new File(savePath);
	        if (!folder.exists()) folder.mkdirs(); 

	        file.transferTo(new File(savePath + uuidName));
	    } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    }

	    return dbPath;
	}
	
	//insert하기
	public void addKit(KitDTO kitDto,
			MultipartFile file1, 
            MultipartFile file2, 
            MultipartFile detailFile){
		
		//기존꺼 빼고 이미지 이름만 uuid바꿔 저장
		kitDto.setMainImage1(uploadFile(file1));
		kitDto.setMainImage2(uploadFile(file2));
		kitDto.setDetailImage(uploadFile(detailFile));
		
		shopMapper.insertKit(kitDto);
	}
	
	//페이지에 해당하는 리스트 가져오기
	public List<KitDTO> getKitListPaged(int offset, int size,String sort) {
	    return shopMapper.getKitListPaged(offset, size,sort);
	}
	//키트 전체 갯수 가져오기
	public long getTotalKitCount() {
	    return shopMapper.getTotalKitCount();
	}
	//게시물 하나 들고오기
	public KitDTO getDetail(long id){
		return shopMapper.getDetail(id);
	}
	//게시물 지우기
	public void deleteShop(long id){
		shopMapper.deleteShop(id);
	};
	
	//업데이트 하기
	public void updateKit( KitDTO kitDto,
									MultipartFile file1, 
						            MultipartFile file2, 
						            MultipartFile detailFile) {
		// 1. 기존 데이터(원본)를 DB에서 먼저 싹 긁어온다
	    KitDTO origin = shopMapper.getDetail(kitDto.getKitId());

	    // 메인이미지 -null이면 기존이름 꽂고 아니면 메서드활용해서 이름넣기
	    if (file1 == null || file1.isEmpty()) {
	        kitDto.setMainImage1(origin.getMainImage1());
	    } else {
	        kitDto.setMainImage1(uploadFile(file1));
	    }

	    //메인 이미지 2 처리
	    if (file2 == null || file2.isEmpty()) {
	        kitDto.setMainImage2(origin.getMainImage2());
	    } else {
	        kitDto.setMainImage2(uploadFile(file2));
	    }

	    //상세 이미지 처리
	    if (detailFile == null || detailFile.isEmpty()) {
	        kitDto.setDetailImage(origin.getDetailImage());
	    } else {
	        kitDto.setDetailImage(uploadFile(detailFile));
	    }

	    // 5. 이제 완전체가 된 kitDto를 Mapper로 던져서 한 번에 Update!
	    shopMapper.updateKit(kitDto);
	}
}
