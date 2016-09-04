package com.hcq.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.hcq.vote.entity.VoteItem;
import com.hcq.vote.entity.VoteOption;
import com.hcq.vote.entity.VoteSubject;
import com.hcq.vote.entity.VoteUser;

public interface VoteService {
	
	
	
	@SuppressWarnings("rawtypes")
	public List getUserCountPerSubject(Long id) throws Exception;
	
	/**��ȡ���е�ͶƱ����
	 * @return
	 * @throws Exception 
	 */
	public List<VoteSubject>getAllSubjects() throws Exception;
	
	/**��������ı����������  1�������ݿ�ȥ��   2 ʹ��lucene
	 * @param title
	 * @return
	 * @throws Exception 
	 */
	public List<VoteSubject>getSubjectsByTitle(String title) throws Exception;
	
	/**
	 * 
	 *��������ı�Ų���������� 
	 * @param id
	 * @param lock:     select *from xxx  where id =xxx for update
	 * @return
	 * @throws Exception 
	 */
	public VoteSubject findSubjectById(long id,boolean lock) throws Exception;
	
	/**����id��ѡ��
	 * @param id
	 * @return
	 * @throws Exception 
	 */
	public VoteOption findOptionById(long id) throws Exception;
	
	/**���»��Ǳ�������
	 * @param subject     subject ����id   ���Ǹ��²���   û��id��������Ӳ���
	 * @throws SQLException 
	 * @throws IOException 
	 */
	public void saveOrUpdate(VoteSubject subject) throws SQLException, IOException;
	
	/**����id
	 * ɾ�����⣨��ɾ��ͶƱ����ɾ��ѡ�������ɾ������
	 * @param id
	 * @throws SQLException 
	 */
	public void deleteSubject(long id) throws SQLException;
	
	/**����id�����û�
	 * @param id
	 * @return
	 * @throws Exception 
	 */
	public VoteUser findUserById(String uname) throws Exception;
	
	/**��������ͶƱ��
	 * @param item
	 * @throws SQLException 
	 */
	public void saveOrUpdate(VoteItem item) throws SQLException;
	
	/**ĳ�������ÿ��ͶƱ��
	 * @param entityId
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("rawtypes")
	public List statVoteCountPerOptionOfSubject(long entityId) throws Exception;
	
	/**������Ǹ����û�
	 * @param user
	 * @throws SQLException 
	 */
	public void saveOrUpdate(VoteUser user) throws SQLException;

	public List<VoteOption> findAllOption(Long vsid) throws Exception;

	public boolean isUserVote(Integer uid, long vsid) throws SQLException, IOException;

	public void saveVoteItem(long vsid, List<Long> chooseIds, Integer uid) throws SQLException;
}
