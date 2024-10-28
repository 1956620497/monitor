package c.e.service;

import c.e.entity.dto.Client;
import c.e.entity.vo.request.*;
import c.e.entity.vo.response.*;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

//注册客户端
public interface ClientService extends IService<Client> {

    //将token给前端
    String registerToken();

    //根据id查找客户端
    Client findClientById(int id);

    //根据token查找客户端
    Client findClientByToken(String token);

    //验证并注册
    boolean verifyAndRegister(String token);

    //保存上报数据
    void updateClientDetail(ClientDetailVO vo,Client client);

    //实时上报数据
    void updateRuntimeDetail(RuntimeDetailVO vo,Client client);

    //获取主机列表
    List<ClientPreviewVO> listClients();

    //获取简单的主机数据
    List<ClientSimpleVO> listSimpleList();

    //修改主机名字
    void renameClient(RenameClientVO vo);

    //获取主机详细信息
    ClientDetailsVO clientDetails(int clientId);

    //更改节点信息
    void renameNode(RenameNodeVO vo);

    //获取历史的数据，包含每一个时间节点的数据
    RuntimeHistoryVO clientRuntimeDetailsHistory(int clientId);

    //获取当前的数据
    RuntimeDetailVO clientRuntimeDetailsNow(int clientId);

    //删除主机
    void deleteClient(int clientId);

    //保存SSH连接信息
    void saveClientSshConnection(SshConnectionVO vo);

    //获取SSH连接信息
    SshSettingsVO sshSettings(int clientId);


}
