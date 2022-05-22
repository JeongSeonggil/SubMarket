package com.submarket.sellerservice.service.impl;

import com.submarket.sellerservice.dto.SellerDto;
import com.submarket.sellerservice.dto.SellerDto;
import com.submarket.sellerservice.jpa.SellerRepository;
import com.submarket.sellerservice.jpa.entity.SellerEntity;
import com.submarket.sellerservice.mapper.SellerMapper;
import com.submarket.sellerservice.service.ISellerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service(value = "SellerService")
@RequiredArgsConstructor
@Slf4j
public class SellerService implements ISellerService {
    private final SellerRepository sellerRepository;
    private final SellerCheckService sellerCheckService;
    private final BCryptPasswordEncoder passwordEncoder;

    /** 사업자 회원가입 */
    @Override
    public int createSeller(SellerDto SellerDto) throws Exception {
        log.info(this.getClass().getName() + ".createSeller Start!");
        String sellerId = SellerDto.getSellerId();
        String sellerEmail = SellerDto.getSellerEmail();
        String businessId = SellerDto.getBusinessId();

        if (sellerCheckService.checkSellerBySellerId(sellerId)) {
            if (sellerCheckService.checkSellerBySellerEmail(sellerEmail)) {
                if (sellerCheckService.checkSellerByBusinessId(businessId)) {
                    // All pass, 회원가입 로직 실행
                    SellerDto.setSellerEncPassword(passwordEncoder.encode(SellerDto.getSellerPassword()));
                    SellerDto.setSellerStatus(1);
                    SellerEntity sellerEntity = SellerMapper.INSTANCE.SellerDtoToSellerEntity(SellerDto);
                    sellerRepository.save(sellerEntity);

// TODO: 2022/05/12 Exception 변경
                } else {
                    throw new RuntimeException("사업자 번호 중복");
                }
            } else {
                throw new RuntimeException("이메일 중복");
            }
        } else {
            throw new RuntimeException("아이디 중복");
        }
        log.info(this.getClass().getName() + ".createSeller End!");

        return 1;
    }

    @Override
    public int deleteSeller(SellerDto SellerDto) throws Exception {
        log.info(this.getClass().getName() + ".deleteSeller Start!");

        if (sellerCheckService.checkSellerBySellerPassword(SellerDto)) {
            // 일치한다면 진행
            SellerEntity sellerEntity = sellerRepository.findBySellerId(SellerDto.getSellerId());

            if (sellerEntity.getSellerStatus() == 1) {
                // 활성화 되어 있다면 탈퇴, Exception
                sellerRepository.changeSellerStatus(sellerEntity.getSellerSeq());
            } else {
                throw new RuntimeException("이미 탈퇴한 회원입니다");
            }
        } else {
            throw new UsernameNotFoundException("비밀번호 불일치");
        }


        log.info(this.getClass().getName() + ".deleteSeller End!");
        return 1;
    }




    //####################################### JWT Don't change #######################################//
    @Override
    public SellerDto getSellerDetailsByUserId(String sellerId) {
        SellerEntity rEntity = sellerRepository.findBySellerId(sellerId);

        if (rEntity == null) {
            throw new UsernameNotFoundException(sellerId);
        }

        // Status 확인
        if (rEntity.getSellerStatus() == 0) {
            throw new UsernameNotFoundException("탈퇴한 회원");
        }
        SellerDto rDTO = SellerMapper.INSTANCE.sellerEntityToSellerDto(rEntity);

        return rDTO;
    }

    @Override
    public UserDetails loadUserByUsername(String sellerId) throws UsernameNotFoundException {
        log.info("sellerName : " + sellerId);
        SellerEntity rEntity = sellerRepository.findBySellerId(sellerId);

        if (rEntity == null) {
            throw new UsernameNotFoundException(sellerId);
        }

        return new User(rEntity.getSellerId(), rEntity.getSellerPassword(),
                true, true, true, true,
                new ArrayList<>());
    }
}
