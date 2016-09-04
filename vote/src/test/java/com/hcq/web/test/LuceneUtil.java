package com.hcq.web.test;

import java.io.File;
import java.io.FileNotFoundException;  
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;  
import org.apache.lucene.document.Field;  
import org.apache.lucene.document.TextField;  
import org.apache.lucene.document.Field.Store;  
import org.apache.lucene.index.DirectoryReader;  
import org.apache.lucene.index.IndexReader;  
import org.apache.lucene.index.IndexWriter;  
import org.apache.lucene.index.IndexWriterConfig;  
import org.apache.lucene.index.Term;  
import org.apache.lucene.index.IndexWriterConfig.OpenMode;  
import org.apache.lucene.queryparser.classic.ParseException;  
import org.apache.lucene.queryparser.classic.QueryParser;  
import org.apache.lucene.search.IndexSearcher;  
import org.apache.lucene.search.Query;  
import org.apache.lucene.search.ScoreDoc;  
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.store.Directory;  
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.chenlb.mmseg4j.analysis.MMSegAnalyzer;
import com.hcq.service.VoteService;
import com.hcq.service.impl.VoteServiceImpl;
import com.hcq.vote.entity.VoteSubject;  
  
/** 
 * Lucene5.0�汾 ����IndexWriter��IndexReaderʱ������Ҫָ���汾 ͬʱ �ڵײ�Ҳ���������������ķ�ʽ 
 * �����Ҫ��ȡ֮ǰ�汾���������� ��������lucene-backward-codecs-5.0.0.jar��  
 * @author Daniel Chiu 
 * 
 */  
public class LuceneUtil  
{  
    private static Directory directory;  
    private IndexWriter writer;  
    private IndexReader reader;  
    private VoteService vss = new VoteServiceImpl();
    static  
    {  
        try  
        {  
            // ��ȡӲ���ϵ�������Ϣ  
            directory = FSDirectory.open(new File("D://index"));  
            // ��ȡ�ڴ��е�������Ϣ ��Ϊ�����ڴ��� ���Բ���Ҫָ�������ļ���  
            // directory = new RAMDirectory();  
        } catch (IOException e)  
        {  
            e.printStackTrace();  
        }  
    }  
  
    /** 
     * ��Directory���ɾ�̬���� ���ڻ�ȡ 
     *  
     * @return 
     */  
    public static Directory getDirectory()  
    {  
        return directory;  
    }  
  
    /** 
     * ��ȡIndexWriter���� 
     *  
     * @return 
     */  
    public IndexWriter getWriter(OpenMode createOrAppend)  
    {  
        if (writer != null)  
            return writer;  
  
        Analyzer analyzer = new MMSegAnalyzer();  
        IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_4_10_4, analyzer);  
        if (createOrAppend == null)  
            // Ĭ�ϲ���Ϊ�½�����  
            conf.setOpenMode(OpenMode.CREATE);  
        else  
            conf.setOpenMode(createOrAppend);  
  
        try  
        {  
            writer = new IndexWriter(directory, conf);  
            return writer;  
        } catch (IOException e)  
        {  
            e.printStackTrace();  
        }  
        return null;  
    }  
  
    public IndexReader getIndexReader()  
    {  
        try  
        {  
            DirectoryReader newReader = null;  
            // �ж�reader�Ƿ�Ϊ�� ��Ϊ�վʹ���һ���µ�reader  
            if (reader == null)  
                reader = DirectoryReader.open(directory);  
            else  
                // ����Ϊ�� �鿴�����ļ��Ƿ����ı� ��������ı�����´���reader  
                newReader = DirectoryReader.openIfChanged((DirectoryReader) reader);  
            if (newReader != null)  
                reader = newReader;  
            return reader;  
        } catch (IOException e)  
        {  
            e.printStackTrace();  
        }  
        return null;  
  
    }  
  
    /** 
     * ��ȡIndexSearcher���� 
     *  
     * @return 
     */  
    public IndexSearcher getIndexSearcher()  
    {  
        return new IndexSearcher(getIndexReader());  
    }  
  
    /** 
     * �������� �м���������Ҫ��� 1. Directory���������ݿ��еı� 2. Document���������ݿ��һ����¼ 3. Field���������ݿ���һ����¼��ĳһ�� 
     * @throws Exception 
     */  
    public void index() throws Exception  
    {  
        Document document = null;  
        writer = getWriter(OpenMode.CREATE);  
  
        // ������Ҫ�������ļ����ļ���  
        List<VoteSubject>list = vss.getAllSubjects();
        // ������Ҫ���������ļ���  
        for (VoteSubject vo:list)  
        {  
            document = new Document();  
            try  
            {  
            	//Store.YESָ�洢�ֶΣ�Ҳ��������ĳЩ�ֶ�Ϊ���洢�����ǿ�������������Store.NO ���ǲ��洢�Ͳ���ֱ�ӻ�ȡ���ֶε����ݡ�
                Field field = new TextField("title",vo.getTitle(),Store.YES);  
                document.add(field);  
                Field field1 = new TextField("vsid",String.valueOf(vo.getVsid()),Store.YES);  
                document.add(field1);  
                Field field2 = new TextField("optioncount",String.valueOf(vo.getOptioncount()),Store.YES);  
                document.add(field2); 
                Field field3 = new TextField("usercount",String.valueOf(vo.getUsercount()),Store.YES);  
                document.add(field3); 
                if (writer.getConfig().getOpenMode() == OpenMode.CREATE)  
                {  
                    System.out.println("adding " + vo);  
                    writer.addDocument(document);  
                } else  
                {  
                    System.out.println("updating " + vo);  
                    writer.updateDocument(new Term("path", vo.toString()), document);  
                }  
            } catch (FileNotFoundException e)  
            {  
                e.printStackTrace();  
            } catch (IOException e)  
            {  
                e.printStackTrace();  
            }  
        }  
        try  
        {  
            // �������ʱ���������� һ��Ҫ�ǵùر�writer ��ȻҲ���Խ�writer��Ƴɵ�����  
            if (writer != null)  
                writer.close();  
        } catch (IOException e)  
        {  
            e.printStackTrace();  
        }  
    }  
  
    public List<VoteSubject> search(String queryStr, int num) throws InvalidTokenOffsetsException  
    {  
    	Analyzer analyzer = new MMSegAnalyzer();  
        //ʹ�ò�ѯ������                                                            Ҫ��ѯ���ֶ�
        QueryParser parser = new QueryParser("title", analyzer);  
        //����������
        IndexSearcher searcher = getIndexSearcher();  
        
        List<VoteSubject>list=new ArrayList<VoteSubject>();
        try  
        {  
            Query query = parser.parse(queryStr);  
            //TopDocs ָ����ƥ�������������ǰN���������������ָ��ļ�����ָ�����ǵ��������������ĵ���
            TopDocs docs = searcher.search(query, num);  
            System.out.println("һ�����������:" + docs.totalHits + "��");
            
            /**�Զ����ע�����ı���ǩ*/ 
            SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<span style='color:red;'>", "</span>");  
            /**������*/
            QueryScorer scorer=new QueryScorer(query);  
            /**����Fragmenter*/
            Fragmenter fragmenter = new SimpleSpanFragmenter(scorer);  
            //����������
            Highlighter highlight=new Highlighter(formatter,scorer);  
            highlight.setTextFragmenter(fragmenter);  
          
            
            Set<String> fieldSet = new HashSet<String>();  
            fieldSet.add("vsid"); 
            fieldSet.add("title");
            fieldSet.add("usercount");
            fieldSet.add("optioncount");
            for (ScoreDoc scoreDoc : docs.scoreDocs)  
            {  
            	//�����ֶ�������ȡ�ĵ�����
            	Document document = searcher.doc(scoreDoc.doc,fieldSet); 
            	VoteSubject voteSubject=new VoteSubject();
                System.out.println("title===="+document.get("title"));
                System.out.println("vsid===="+document.get("vsid"));
                
                //������ʾ
            	String str = highlight.getBestFragment(new MMSegAnalyzer(), "title",document.get("title"));
                
            	voteSubject.setTitle(str);
                voteSubject.setVsid(Long.valueOf(document.get("vsid")));
                voteSubject.setOptioncount(Integer.valueOf(document.get("optioncount")));
                voteSubject.setUsercount(Integer.valueOf(document.get("usercount")));
                list.add(voteSubject);
            }  
        } catch (ParseException e)  
        {  
            e.printStackTrace();  
        } catch (IOException e)  
        {  
            e.printStackTrace();  
        }  
        return list;
    }  
}  
