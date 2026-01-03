package com.ktg.mes.report.bean;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.util.StringUtil;
import com.ktg.mes.pro.domain.ProWorkorder;
import com.ktg.mes.pro.service.IProWorkorderService;
import com.ktg.mes.report.utils.QRCodeUtil;  // 需要添加这个import
import com.ktg.mes.wm.domain.WmBarcode;
import com.ktg.mes.wm.service.IWmBarcodeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Component
@Slf4j
public class PmReportBean {

    @Autowired
    private IProWorkorderService proWorkorderService;

    @Autowired
    private IWmBarcodeService wmBarcodeService;
    
    @Autowired
    private QRCodeUtil qrCodeUtil; // 注入工具类

    public List<ProWorkorder> getData(String dsName, String datasetName, Map<String, Object> parameters) {

        String id = MapUtils.getString(parameters, "id");
        if (StringUtil.isEmpty(id)) {
            id = "0";
        }
        ProWorkorder proWorkorder = proWorkorderService.selectProWorkorderByWorkorderId(Long.parseLong(id));

        List<ProWorkorder> list = new ArrayList<>();
        list.add(proWorkorder);
        log.info("PmReportBean dsName :" + dsName + ", datasetName :" + datasetName + ", resp:" + JSON.toJSONString(list));
        return list;
    }

    public List<ProWorkorder> getChildData(String dsName, String datasetName, Map<String, Object> parameters) {

        String id = MapUtils.getString(parameters, "id");
        if (StringUtil.isEmpty(id)) {
            id = "0";
        }
        List<ProWorkorder> proWorkorders = proWorkorderService.selectProWorkorderListByParentId(Long.parseLong(id));

        List<ProWorkorder> list = new ArrayList<>();
        log.info("PmReportBean dsName :" + dsName + ", datasetName :" + datasetName + ", resp:" + JSON.toJSONString(list));
        return proWorkorders;
    }

    public List<WmBarcode> getQc(String dsName, String datasetName, Map<String, Object> parameters) {

        String id = MapUtils.getString(parameters, "id");
        if (StringUtil.isEmpty(id)) {
            id = "0";
        }
        WmBarcode wmBarcode = new WmBarcode();
        wmBarcode.setBarcodeId(Long.parseLong(id));

        List<WmBarcode> codeList = wmBarcodeService.selectWmBarcodeList(wmBarcode);
        List<WmBarcode> list = new ArrayList<>();

        if(!CollectionUtils.isEmpty(codeList)){
            WmBarcode barcode = codeList.get(0);
            
            // 关键修改：生成一个指向内部接口的简单URL
            // 这个URL会被UReport当作普通网络图片处理，但因为它指向我们自己的服务，所以速度极快
            String simpleImageUrl = "http://localhost:8080/api/qrcode?content=" + barcode.getBarcodeContent();
            barcode.setBarcodeUrl(simpleImageUrl);
            
            list.add(barcode);
        }
        log.info("PmReportBean dsName :" + dsName + ", datasetName :" + datasetName + ", resp:" + JSON.toJSONString(list));
        return list;
    }
}