package com.submarket.userservice.controller;

import com.submarket.userservice.dto.SubDto;
import com.submarket.userservice.jpa.entity.SubEntity;
import com.submarket.userservice.mapper.SubMapper;
import com.submarket.userservice.service.impl.SubService;
import com.submarket.userservice.util.TokenUtil;
import com.submarket.userservice.vo.RequestSub;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@RestController
@Slf4j
@RequiredArgsConstructor
public class SubController {
    private final SubService subService;
    private final TokenUtil tokenUtil;

    @GetMapping("/sub")
    public ResponseEntity<Map<String, Object>> findAllSub(@RequestHeader HttpHeaders headers) throws Exception {
        log.info(this.getClass().getName() + ".findSub Start!");

        Map<String, Object> rMap = new HashMap<>();

        String userId = tokenUtil.getUserIdByToken(headers);


        SubDto subDto = new SubDto();
        subDto.setUserId(userId);
        List<SubEntity> subEntityList = subService.findAllSub(subDto);

        List<SubDto> subDtoList = new ArrayList<>();

        subEntityList.forEach(subEntity -> {
            subDtoList.add(SubMapper.INSTANCE.subEntityToSubDto(subEntity));
        });

        rMap.put("response", subDtoList);

        return ResponseEntity.ok().body(rMap);



    }

    @GetMapping("/sub/{subSeq}")
    public ResponseEntity<SubDto> findOneSub(@PathVariable int subSeq) throws Exception {
        log.info(this.getClass().getName() + ".findOneSub Start!");
        SubDto pDto = new SubDto();

        pDto.setSubSeq(subSeq);

        SubDto subDto = subService.findOneSub(pDto);

        if (subDto == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }


        log.info(this.getClass().getName() + ".findOneSub Start!");

        return ResponseEntity.ok().body(subDto);
    }

    @PostMapping("/sub")
    public ResponseEntity<String> createNewSub(@RequestHeader HttpHeaders headers,
                                               @RequestBody SubDto subDto) throws Exception{
        log.info(this.getClass().getName() + ".createNewSub Start!");

        String userId = tokenUtil.getUserIdByToken(headers);

        subDto.setUserId(userId);


        int res = subService.createNewSub(subDto);

        if (res == 2) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("????????? ??????");
        }

        if (res != 1) {
            return ResponseEntity.status(500).body("??????");
        }

        log.info(this.getClass().getName() + ".createNewSub End! ");

        return ResponseEntity.status(HttpStatus.CREATED).body("?????? ??????");
    }

    @PostMapping("/sub/delete")
    public String cancelSub(@RequestBody RequestSub requestSub) throws Exception {
        log.info(this.getClass().getName() + "cancel Sub Start!");

        SubDto subDto = new SubDto();

        subDto.setSubSeq(requestSub.getSubSeq());

        int res = subService.cancelSub(subDto);


        log.info(this.getClass().getName() + "cancel Sub End!");

        if (res != 1) {
            return "?????? ?????? ??????";
        }
        return "?????? ?????? ??????";
    }

    @PostMapping("/sub/update")
    public ResponseEntity<String> updateSub(@RequestBody RequestSub requestSub) throws Exception {
        log.info(this.getClass().getName() + ".updateSub Start!");
        SubDto subDto = new SubDto();
        subDto.setSubSeq(requestSub.getSubSeq());

        int res = subService.updateSub(subDto);

        if (res != 1) {
            return ResponseEntity.ok("?????? ??????");
        }

        log.info(this.getClass().getName() + "updateSub End!");
        return ResponseEntity.ok("?????? ??????");


    }

    @GetMapping("/seller/sub")
    public ResponseEntity<Integer> findSubCount(@RequestBody Map<String, Object> request)  throws Exception {
        // Seller ??? ???????????? ?????? ????????? SeqList ??? ???????????? ??? ?????? ?????? ??????
        log.info(this.getClass().getName() + "findSubCount");
        List<Integer> itemSeqList = new LinkedList<>();
        itemSeqList = (List<Integer>) request.get("itemSeqList");

        int count = subService.findSubCount(itemSeqList);

        return ResponseEntity.status(HttpStatus.OK).body(count);
    }

    @GetMapping("/seller/sub/{itemSeq}")
    public ResponseEntity<Integer> findOneSubCount(@PathVariable int itemSeq) throws Exception {
        log.info(this.getClass().getName() + "findOneSubCount Start!");

        int count = subService.findOneSubCount(itemSeq);

        log.info(this.getClass().getName() + "findOneSubCount End!");

        return ResponseEntity.ok().body(count);
    }
}
