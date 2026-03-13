package BusinessService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nextage.web.domain.BusinessDTO;
import com.nextage.web.mapper.BusinessMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BusinessService {
    private final BusinessMapper businessMapper;
    private final PasswordEncoder passwordEncoder;

//    public void register(BusinessDTO business) {
//        business.setPasswordHash(passwordEncoder.encode(business.getPasswordHash()));
//        businessMapper.insertBusiness(business);
//    }
//
//    public void modify(BusinessDTO business) {
//        businessMapper.updateBusiness(business);
//    }
}
