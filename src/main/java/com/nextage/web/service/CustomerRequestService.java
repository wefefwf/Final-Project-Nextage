package com.nextage.web.service;

import com.nextage.web.domain.AttachmentDTO;
import com.nextage.web.domain.RequestDTO;
import com.nextage.web.mapper.CustomerRequestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.util.List;
import java.util.UUID;

@Service
public class CustomerRequestService {

    @Autowired
    private CustomerRequestMapper requestMapper;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Transactional
    public void registerRequest(RequestDTO dto, MultipartFile[] files) {
        requestMapper.insertRequest(dto);

        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            String[] tagArray = dto.getTags().split(",");
            for (String tag : tagArray) {
                String trimmedTag = tag.trim();
                if (!trimmedTag.isEmpty()) {
                    requestMapper.insertTag(dto.getRequestId(), trimmedTag);
                }
            }
        }

        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i] != null && !files[i].isEmpty()) {
                    String originalName = files[i].getOriginalFilename();
                    String savedName = UUID.randomUUID().toString() + "_" + originalName;
                    try {
                        files[i].transferTo(new File(uploadDir + "/" + savedName));
                        AttachmentDTO attach = new AttachmentDTO();
                        attach.setRefType("REQ");
                        attach.setRefId(dto.getRequestId());
                        attach.setImageUrl("/images/" + savedName);
                        attach.setOriginName(originalName);
                        attach.setThumbnail(i == 0);
                        requestMapper.insertAttachment(attach);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // ✅ 수정
    @Transactional
    public void updateRequest(RequestDTO dto, MultipartFile[] files) {
        requestMapper.updateRequest(dto);

        requestMapper.deleteTagsByRequestId(dto.getRequestId());
        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            for (String tag : dto.getTags().split(",")) {
                String trimmed = tag.trim();
                if (!trimmed.isEmpty()) {
                    requestMapper.insertTag(dto.getRequestId(), trimmed);
                }
            }
        }

        if (files != null && files.length > 0 && !files[0].isEmpty()) {
            requestMapper.deleteAttachmentsByRequestId(dto.getRequestId());
            for (int i = 0; i < files.length; i++) {
                if (!files[i].isEmpty()) {
                    String originalName = files[i].getOriginalFilename();
                    String savedName = UUID.randomUUID().toString() + "_" + originalName;
                    try {
                        files[i].transferTo(new File(uploadDir + "/" + savedName));
                        AttachmentDTO attach = new AttachmentDTO();
                        attach.setRefType("REQ");
                        attach.setRefId(dto.getRequestId());
                        attach.setImageUrl("/images/" + savedName);
                        attach.setOriginName(originalName);
                        attach.setThumbnail(i == 0);
                        requestMapper.insertAttachment(attach);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public List<RequestDTO> getAllRequests() {
        return requestMapper.selectAllRequests();
    }

    public RequestDTO getRequestDetail(Long requestId) {
        return requestMapper.selectRequestDetail(requestId);
    }

    public List<RequestDTO> getRequestsByCustomerId(Long customerId) {
        return requestMapper.selectRequestsByCustomerId(customerId);
    }

    // ✅ 고객용 상태 변경 (본인 확인 포함)
    @Transactional
    public void updateRequestStatus(Long requestId, String status, Long currentCustomerId) {
        RequestDTO request = requestMapper.selectRequestDetail(requestId);

        if (request != null && request.getCustomerId().equals(currentCustomerId)) {
            requestMapper.updateStatus(requestId, status);
        } else {
            throw new RuntimeException("해당 의뢰의 상태를 변경할 권한이 없습니다.");
        }
    }

    // ✅ 업체용 상태 변경 (수락/거절/완료)
    @Transactional
    public void updateRequestStatusByBusiness(Long requestId, String status) {
        RequestDTO request = requestMapper.selectRequestDetail(requestId);
        if (request == null) {
            throw new RuntimeException("존재하지 않는 의뢰입니다.");
        }
        requestMapper.updateStatus(requestId, status);
    }

    @Transactional
    public void removeRequest(Long requestId) {
        requestMapper.deleteTagsByRequestId(requestId);
        requestMapper.deleteAttachmentsByRequestId(requestId);
        requestMapper.deleteRequest(requestId);
    }

    public List<RequestDTO> getUrgentRequests() {
        return requestMapper.selectUrgentRequests();
    }

    public List<RequestDTO> getNewRequests() {
        return requestMapper.selectNewRequests();
    }

    public List<RequestDTO> getBestReviews() {
        return requestMapper.selectBestReviews();
    }

    public List<RequestDTO> getRequestsByCategory(String category) {
        return requestMapper.selectRequestsByCategory(category);
    }
}