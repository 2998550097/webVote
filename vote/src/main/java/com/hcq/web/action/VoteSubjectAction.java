package com.hcq.web.action;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.InterceptorRefs;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import com.hcq.service.VoteService;
import com.hcq.service.impl.VoteServiceImpl;
import com.hcq.vote.entity.JsonModel;
import com.hcq.vote.entity.VoteItem;
import com.hcq.vote.entity.VoteOption;
import com.hcq.vote.entity.VoteSubject;
import com.hcq.vote.entity.VoteUser;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;


@Namespace("/")
@ParentPackage("mypackge")
@InterceptorRefs({@InterceptorRef("defaultStack"),@InterceptorRef(value="log",params={"excludeMethods","getLoginUser"}),@InterceptorRef(value="index")
})
public class VoteSubjectAction extends ActionSupport implements ModelDriven<VoteSubject> {
	private static final long serialVersionUID = -6551447186945016743L;
	private VoteSubject voteSubject;
	private VoteService vss = new VoteServiceImpl();
	private JsonModel jsonModel;
	
	private HttpSession session;
	
	
	public VoteSubjectAction() {
		session =ServletActionContext.getRequest().getSession();
	}
	
	@Action(value="/voteSubject_findAll",results=@Result(type="json",name="success",params={"root","jsonModel","excludeNullProperties","true","noCache","true"}))
	public String findAll(){
		jsonModel = new JsonModel();
		try{
			List<VoteSubject>list = vss.getAllSubjects();
			session.setAttribute("subjectlist", list);
			jsonModel.setCode(1);
			jsonModel.setObj(list);
		}catch(Exception e){
			e.printStackTrace();
			jsonModel.setCode(0);
			jsonModel.setMsg(e.getMessage());
		}
		return ActionSupport.SUCCESS;
	}
	
	@Action(value="/voteSubject_getLoginUser",results=@Result(type="json",name="success",params={"root","jsonModel","excludeNullProperties","true","noCache","true"}))
	public String getLoginUser(){
		jsonModel = new JsonModel();
		if(session.getAttribute("loginUser")!=null){
			VoteUser vu = (VoteUser)session.getAttribute("loginUser");
			jsonModel.setCode(1);
			jsonModel.setObj(vu);
		}else{
			jsonModel.setCode(0);
		}
		return ActionSupport.SUCCESS;
	}
	
	@SuppressWarnings("unchecked")
	@Action(value="/voteSubject_findByVsid",results=@Result(type="json",name="success",params={"root","jsonModel","excludeNullProperties","true","noCache","true"}))
	public String findByVsid(){
		jsonModel = new JsonModel();
		List<VoteSubject>subjectlist=(List<VoteSubject>)session.getAttribute("subjectlist");
		Long vsid = voteSubject.getVsid();
		VoteSubject vs = null;
		for(VoteSubject voteSubject : subjectlist){
			if( voteSubject.getVsid() == vsid.longValue()){
				vs = voteSubject;
				break;
			}
		}
		try{
				//���vs������ѡ���ĳ�������ѡ��
				List<VoteOption>optionlist=vss.findAllOption(vsid);
				jsonModel.setCode(1);
				vs.setOptions(optionlist);
				//�����ڲ鿴�������Լ���������µ�����ѡ��
				session.setAttribute("votesubject", vs);
				jsonModel.setObj(vs);
			}catch(Exception e){
				e.printStackTrace();
				jsonModel.setCode(0);
				jsonModel.setMsg(e.getMessage());
			}
		return ActionSupport.SUCCESS;
	}
	
	
	@SuppressWarnings("unchecked")
	@Action(value="/voteSubject_findByVsidWithOptionInfo",results=@Result(type="json",name="success",params={"root","jsonModel","excludeNullProperties","true","noCache","true"}))
	public String findByVsidOptionInfo(){
		jsonModel = new JsonModel();
		try{
			VoteSubject vs = (VoteSubject)session.getAttribute("votesubject");
			List<VoteItem>list = vss.statVoteCountPerOptionOfSubject(vs.getVsid());
			for(VoteItem vi:list){
				for( int i=0;i<vs.getOptions().size();i++){
					VoteOption vo = vs.getOptions().get(i);
					if(vo.getVoteid() == vi.getVoteid()){
						vo.setVotecount(vi.getVotecount());
						vs.getOptions().set(i,vo);
					}
				}
			}
			jsonModel.setCode(1);
			jsonModel.setObj(vs);
		}catch(Exception e){
			e.printStackTrace();
			jsonModel.setCode(0);
			jsonModel.setObj(e.getMessage());
		}
		return ActionSupport.SUCCESS;
	}
		
	
	@Action(value="/voteSubject_vote",results=@Result(type="json",name="success",params={"root","jsonModel","excludeNullProperties","true","noCache","true"}))
	public String vote(){
		VoteSubject vs = (VoteSubject)session.getAttribute("votesubject");
		VoteUser loginUser =(VoteUser)session.getAttribute("loginUser");
		jsonModel =new JsonModel();
		
		try{
			//�鿴��ǰ�û��Ƿ����һ��ͶƱ��~
			boolean flag = vss.isUserVote(loginUser.getUid(),vs.getVsid());
			if(flag){
				jsonModel.setCode(0);
				jsonModel.setMsg("you have vote once!");
				return ActionSupport.SUCCESS;
			}
		List<Long>chooseIds=voteSubject.getChooseIds();
		if(chooseIds==null||chooseIds.size()<=0){
			jsonModel.setCode(0);
			jsonModel.setMsg("please choose at least one choice !");
			return ActionSupport.SUCCESS;
		}
		vss.saveVoteItem(vs.getVsid(),chooseIds,loginUser.getUid());
		jsonModel.setCode(1);
		
		//���޸�session��votesubject�еĶ�Ӧ���û�����ÿ��ѡ���ͶƱ����
		//TODO:Ӧ����Ҫ���²����ݿ⣬�޷�����߲���
		vs.setUsercount(vs.getUsercount()+1);
		for( Long id:chooseIds){
			for(int i=0;i<vs.getOptions().size();i++){
				VoteOption vo = vs.getOptions().get(i);
				if(id.longValue()==vo.getVoteid()){
					vo.setVotecount(vo.getVotecount()==null?0:vo.getVotecount()+1);
				}
				vs.getOptions().set(i, vo);
			}
		}
		session.setAttribute("votesubject", vs);
		}catch(Exception e){
			e.printStackTrace();
			jsonModel.setCode(0);
			jsonModel.setMsg("erro:"+e.getMessage());
		}
		return ActionSupport.SUCCESS;
	}
	
	@Action(value="/voteSubject_add",results=@Result(type="json",name="success",params={"root","jsonModel","excludeNullProperties","true","noCache","true"}))
	public String add(){
		jsonModel = new JsonModel();
		try{
			vss.saveOrUpdate(voteSubject);
			System.out.println(voteSubject);
			jsonModel.setCode(1);
		}catch(Exception e){
			e.printStackTrace();
			jsonModel.setCode(0);
			jsonModel.setMsg(e.getMessage());
		}
		return ActionSupport.SUCCESS;
	}
	public JsonModel getJsonModel() {
		return jsonModel;
	}

	public void setJsonModel(JsonModel jsonModel) {
		this.jsonModel = jsonModel;
	}

	public VoteSubject getModel() {
		voteSubject = new VoteSubject();
		return voteSubject;
	}
}
