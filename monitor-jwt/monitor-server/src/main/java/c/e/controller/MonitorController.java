package c.e.controller;

import c.e.entity.RestBean;
import c.e.entity.dto.Account;
import c.e.entity.vo.request.RenameClientVO;
import c.e.entity.vo.request.RenameNodeVO;
import c.e.entity.vo.request.RuntimeDetailVO;
import c.e.entity.vo.request.SshConnectionVO;
import c.e.entity.vo.response.*;
import c.e.service.AccountService;
import c.e.service.ClientService;
import c.e.utils.Const;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monitor")
public class MonitorController {

    @Resource
    ClientService service;

    @Resource
    AccountService accountService;

    //获取服务器列表
    @GetMapping("/list")
    public RestBean<List<ClientPreviewVO>> listAllClient(@RequestAttribute(Const.ATTR_USER_ID)int userId,
                                                         @RequestAttribute(Const.ATTR_USER_ROLE) String userRole) {
        //优化没有主机时服务端报错问题
        List<ClientPreviewVO> clients = service.listClients();
        //判断一下该用户是否有权限获取主机
        //判断当前用户是不是管理员
        if (this.isAdminAccount(userRole)){
            //是管理员
            //如果获取数据为空，有可能是没有主机，有可能是出现了异常
            if (clients == null)
                return RestBean.failure(200,"还没有添加新主机哦,如果已添加过主机，服务器可能内部出错，请联系管理员！");
            return RestBean.success(service.listClients());
        }else {
            //不是管理员，用户角色为子账户，对主机列表进行筛选
            List<Integer> ids = this.accountAccessClients(userId);
            //返回筛选后的能自己管理的列表
            return RestBean.success(clients.stream()
                    .filter(vo -> ids.contains(vo.getId()))
                    .toList());
        }
    }

    //主机改名
    @PostMapping("/rename")
    public RestBean<Void> renameClient(@RequestBody @Valid RenameClientVO vo,
                                       @RequestAttribute(Const.ATTR_USER_ID)int userId,
                                       @RequestAttribute(Const.ATTR_USER_ROLE) String userRole){
        if (this.permissionCheck(userId,userRole,vo.getId())){
            service.renameClient(vo);
            return RestBean.success();
        }else {
            return RestBean.noPermission();
        }

    }

    //获取主机详细信息
    @GetMapping("/details")
    public RestBean<ClientDetailsVO> details(int clientId,
                                             @RequestAttribute(Const.ATTR_USER_ID)int userId,
                                             @RequestAttribute(Const.ATTR_USER_ROLE) String userRole){
        if (this.permissionCheck(userId,userRole,clientId)){
            return RestBean.success(service.clientDetails(clientId));
        }else {
            return RestBean.noPermission();
        }

    }

    //更改节点信息
    @PostMapping("/node")
    public RestBean<Void> renameNode(@RequestBody @Valid RenameNodeVO vo,
                                     @RequestAttribute(Const.ATTR_USER_ID)int userId,
                                     @RequestAttribute(Const.ATTR_USER_ROLE) String userRole){
        if (this.permissionCheck(userId,userRole,vo.getId())){
            service.renameNode(vo);
            return RestBean.success();
        }else {
            return RestBean.noPermission();
        }

    }


    //一次性获取过去一个小时数据的接口,也就是历史数据
    @GetMapping("/runtime-history")
    public RestBean<RuntimeHistoryVO> runtimeDetailsHistory(int clientId,
                                                            @RequestAttribute(Const.ATTR_USER_ID)int userId,
                                                            @RequestAttribute(Const.ATTR_USER_ROLE) String userRole){
        if (this.permissionCheck(userId,userRole,clientId)) {
            return RestBean.success(service.clientRuntimeDetailsHistory(clientId));
        }else {
            return RestBean.noPermission();
        }
    }

    //获取当前最新的数据
    @GetMapping("/runtime-now")
    public RestBean<RuntimeDetailVO> runtimeDetailsNow(int clientId,
                                                       @RequestAttribute(Const.ATTR_USER_ID)int userId,
                                                       @RequestAttribute(Const.ATTR_USER_ROLE) String userRole){
            if (this.permissionCheck(userId,userRole,clientId)){
                return RestBean.success(service.clientRuntimeDetailsNow(clientId));
            }else {
                return RestBean.noPermission();
            }

    }

    //前端添加新主机时，返回Token
    @GetMapping("/register")
    public RestBean<String> registerToken(@RequestAttribute(Const.ATTR_USER_ROLE) String userRole){
        //该token只有管理员能看到，普通用户没有权限添加主机
        if (this.isAdminAccount(userRole)){
            return RestBean.success(service.registerToken());
        }else{
            return RestBean.noPermission();
        }

    }

    //前端删除主机
    @GetMapping("/delete")
    public RestBean<String> deleteClient(int clientId,
                                         @RequestAttribute(Const.ATTR_USER_ROLE) String userRole){
        //不是管理员也不允许删除主机
        if (this.isAdminAccount(userRole)){
            service.deleteClient(clientId);
            return RestBean.success();
        }else{
            return RestBean.noPermission();
        }
    }

    //获取主机列表
    @GetMapping("/simple-list")
    public RestBean<List<ClientSimpleVO>> simpleClientList( @RequestAttribute(Const.ATTR_USER_ROLE) String userRole){
        if (this.isAdminAccount(userRole)){
            return RestBean.success(service.listSimpleList());
        }else {
            return RestBean.noPermission();
        }

    }

    //保存SSH连接信息
    @PostMapping("/ssh-save")
    public RestBean<Void> saveSshConnection(@RequestBody @Valid SshConnectionVO vo,
                                            @RequestAttribute(Const.ATTR_USER_ID)int userId,
                                            @RequestAttribute(Const.ATTR_USER_ROLE) String userRole){
        //判断是否有权限，还是只有管理员才能上传ssh连接信息
        if (this.permissionCheck(userId,userRole,vo.getId())){
            service.saveClientSshConnection(vo);
            return RestBean.success();
        }else {
            return RestBean.noPermission();
        }

    }

    //查询SSH连接信息
    @GetMapping("/ssh")
    public RestBean<SshSettingsVO> sshSettings(int clientId,
                                               @RequestAttribute(Const.ATTR_USER_ID)int userId,
                                               @RequestAttribute(Const.ATTR_USER_ROLE) String userRole){
        if (this.permissionCheck(userId,userRole,clientId)){
            return RestBean.success(service.sshSettings(clientId));
        }else {
            return RestBean.noPermission();
        }

    }

    //判断用户是否有权限访问该主机
    //返回一个可管理主机的列表
    private List<Integer> accountAccessClients(int uid){
        Account account = accountService.getById(uid);
        return account.getClientList();
    }

    //判断是不是管理员账户
    //返回用户角色
    private boolean isAdminAccount(String role){
        //spring获取的角色前面会有一个  ROLE_  ,判断的时候需要把这个头去掉
//        System.out.println(role);
        role = role.substring(5);
        return Const.ROLE_ADMIN.equals(role);
    }

    //对主机操作时，判断有没有权限
    //判断身份，判断子账户的权限中是否有这台主机
    private boolean permissionCheck(int uid,String role,int clientId){
        //如果是管理员就不用管
        if (this.isAdminAccount(role)) return true;
        //查询一下允许管理的主机列表是否包含了该uid
        return this.accountAccessClients(uid).contains(clientId);
    }
}
