package com.submarket.sellerservice.controller;

import com.submarket.sellerservice.dto.SellerDto;
import com.submarket.sellerservice.mapper.SellerMapper;
import com.submarket.sellerservice.service.impl.SellerCheckService;
import com.submarket.sellerservice.service.impl.SellerService;
import com.submarket.sellerservice.util.TokenUtil;
import com.submarket.sellerservice.vo.RequestChangePassword;
import com.submarket.sellerservice.vo.RequestSellerInfo;
import com.submarket.sellerservice.vo.ResponseSellerInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SellerController {
    private final SellerService sellerService;
    private final TokenUtil tokenUtil;
    private final SellerCheckService sellerCheckService;


    @GetMapping("/seller")
    public ResponseEntity<ResponseSellerInfo> getSellerInfo(@RequestHeader HttpHeaders headers) throws Exception {
        log.info(this.getClass().getName() + ".getSellerInfo Start!");
        SellerDto pDto = new SellerDto();

        String sellerId = tokenUtil.getUserIdByToken(headers);
        pDto.setSellerId(sellerId);

        SellerDto sellerDto = sellerService.getSellerInfoBySellerId(pDto);

        log.info("seller Service End!");

        ResponseSellerInfo sellerInfo = SellerMapper.INSTANCE.SellerDtoToResponseSellerInfo(sellerDto);

        if (sellerInfo == null) {
            log.info(this.getClass().getName() + "userToken");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }


        log.info(this.getClass().getName() + ".getSellerInfo End!");

        return ResponseEntity.ok().body(sellerInfo);
    }

    @PostMapping("/sellers")
    public ResponseEntity<String> createSeller(@RequestBody RequestSellerInfo sellerInfo) throws Exception {
        log.info(this.getClass().getName() + ".createSeller Start!");

        SellerDto SellerDto = SellerMapper.INSTANCE.requestSellerInfoToSellerDto(sellerInfo);

        int res = sellerService.createSeller(SellerDto);

        if (res == 1) {
            // ???????????? ??????
            return ResponseEntity.status(HttpStatus.CREATED).body("???????????? ??????");
        } else if (res == 2) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("????????? ????????? ?????????");
        } else if (res == 3) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("????????? ????????? ?????????");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("???????????? ??????");
        }
    }

    @PostMapping("/sellers/modify")
    public ResponseEntity<String> modifySellerInfo(@RequestBody  SellerDto sellerDto,
                                                   @RequestHeader HttpHeaders headers) throws Exception {
        log.info(this.getClass().getName() + ".modifySellerInfo Start!");

        String sellerId = tokenUtil.getUserIdByToken(headers);
        sellerDto.setSellerId(sellerId);

        int res = sellerService.modifySellerInfo(sellerDto);

        if (res == 1) {
            log.info("?????? ??????");
            return ResponseEntity.ok().body("?????? ??????");
        }

        log.info("?????? ??????");
        log.info(this.getClass().getName() + ".modifySellerInfo End!");
        return ResponseEntity.ok().body("?????? ?????? (500)");
    }

    @PostMapping("/sellers/drop")
    public ResponseEntity<String> deleteSeller(@RequestHeader HttpHeaders headers,
                                               @RequestBody RequestSellerInfo requestSellerInfo) throws Exception {
        log.info(this.getClass().getName() + ".deleteSeller Start!");
        SellerDto SellerDto = new SellerDto();

        String sellerId = tokenUtil.getUserIdByToken(headers);

        SellerDto.setSellerId(sellerId);
        SellerDto.setSellerPassword(requestSellerInfo.getSellerPassword());
        sellerService.deleteSeller(SellerDto);

        log.info(this.getClass().getName() + ".deleteSeller End!");


        return ResponseEntity.status(HttpStatus.OK).body("?????? ?????? ??????");
    }

    /**
     * <------------------------>????????? ?????? with UserEmail </------------------------>
     * ?????? Email ??? ????????? ????????? ?????? ????????? ??????
     */
    @GetMapping("/sellers/find-id/{sellerEmail}")
    public ResponseEntity<String> findSellerId(@PathVariable String sellerEmail) throws Exception {
        log.info("-------------------- > " + this.getClass().getName() + "findSellerId Start!");
        SellerDto sellerDto = new SellerDto();

        sellerDto.setSellerEmail(sellerEmail);

        sellerDto = sellerService.getSellerInfoBySellerEmail(sellerDto);

        if (sellerDto == null) { /** ?????? ????????? ?????? ?????? Not Found return */
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("???????????? ?????? ??? ????????????.");
        }
        /** ????????? ????????? ?????? ?????? ?????? ??? ?????? */
        String sellerId = sellerDto.getSellerId().replaceAll("(?<=.{4}).", "*");
        log.info("-------------------- > " + this.getClass().getName() + "findUserId End!");
        return ResponseEntity.status(HttpStatus.OK).body(sellerId);
    }

    @PatchMapping("/sellers/change-password")
    public ResponseEntity<String> changePassword(@RequestHeader HttpHeaders headers, @RequestBody RequestChangePassword
            requestChangePassword) throws Exception {
        log.info(this.getClass().getName() + ".changePassword Start!");
        int res = 0;
        String sellerId = tokenUtil.getUserIdByToken(headers);

        res = sellerService.changePassword(requestChangePassword.getOldPassword(),
                requestChangePassword.getNewPassword(),
                sellerId);

        if (res == 0) {
            log.info("???????????? ?????? ??????");
            throw new RuntimeException("???????????? ?????? ??????");
        }

        log.info(this.getClass().getName() + ".changePassword End!");
        return ResponseEntity.ok().body("???????????? ?????? ??????");
    }

    // ????????? ?????? ????????? ??????
    @GetMapping("/seller/business/{businessId}")
    public ResponseEntity<Map<String, Object>> checkBusinessId(@PathVariable String businessId) throws Exception {
        log.info(this.getClass().getName() + ".checkBusinessId Start!");

        Map<String, Object> taxType = sellerCheckService.checkBusinessId(businessId);

        log.info(this.getClass().getName() + ".checkBusinessId End!");
        return ResponseEntity.ok().body(taxType);
    }
}

