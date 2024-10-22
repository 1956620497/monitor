package c.e.controller;

import c.e.entity.RestBean;
import c.e.entity.vo.request.RenameClientVO;
import c.e.entity.vo.request.RenameNodeVO;
import c.e.entity.vo.request.RuntimeDetailVO;
import c.e.entity.vo.response.ClientDetailsVO;
import c.e.entity.vo.response.ClientPreviewVO;
import c.e.entity.vo.response.RuntimeHistoryVO;
import c.e.service.ClientService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monitor")
public class MonitorController {

    @Resource
    ClientService service;

    //获取服务器列表
    @GetMapping("/list")
    public RestBean<List<ClientPreviewVO>> listAllClient() {
        return RestBean.success(service.listClients());
    }

    //主机改名
    @PostMapping("/rename")
    public RestBean<Void> renameClient(@RequestBody @Valid RenameClientVO vo){
        service.renameClient(vo);
        return RestBean.success();
    }

    //获取主机详细信息
    @GetMapping("/details")
    public RestBean<ClientDetailsVO> details(int clientId){
        return RestBean.success(service.clientDetails(clientId));
    }

    //更改节点信息
    @PostMapping("/node")
    public RestBean<Void> renameNode(@RequestBody @Valid RenameNodeVO vo){
        service.renameNode(vo);
        return RestBean.success();
    }


    //一次性获取过去一个小时数据的接口,也就是历史数据
    @GetMapping("/runtime-history")
    public RestBean<RuntimeHistoryVO> runtimeDetailsHistory(int clientId){
        return RestBean.success(service.clientRuntimeDetailsHistory(clientId));
    }

    //获取当前最新的数据
    @GetMapping("/runtime-now")
    public RestBean<RuntimeDetailVO> runtimeDetailsNow(int clientId){
        return RestBean.success(service.clientRuntimeDetailsNow(clientId));
    }

}
