import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class TestLucene {
	private static String m_strIndexDirectory = "/home/eunhye/Desktop/LuceneTest";
	private static String m_strDataDirectory = "/home/eunhye/Desktop/DataTest";
	private static String m_strSerializeDirectory = "/home/eunhye/Desktop/SerializedTest";
	
    private Document getDocument(String strFileName) throws Exception 
    {
    	File f = new File(strFileName);
        Document doc = new Document();

        doc.add(new TextField("contents", new FileReader(f)));
        doc.add(new TextField("filename", f.getAbsolutePath(), Field.Store.YES));
        doc.add(new StringField("fullpath", f.getCanonicalPath(), Field.Store.YES));

        return doc;
    }
    
    private ArrayList<String> getFileList(String strRoot)
    {
    	ArrayList<String> stReturnList = new ArrayList<String>();
    	File stFile = new File(strRoot);
    	do {
    		if(!stFile.exists())
    		{
    			break;
    		}
    		else if(!stFile.canRead())
    		{
    			break;
    		}
    		else if(stFile.isDirectory())
    		{
    			File[] files = stFile.listFiles();
    			for(File f : files)
    			{
    				stReturnList.addAll(getFileList(f.getAbsolutePath()));
    			}
    		}
    		else if(stFile.isFile())
    		{
    			stReturnList.add(stFile.getAbsolutePath());
    		}
    			
    	} while(false);
    	
    	return stReturnList;
    }
    
    private HashMap<String, Integer> getSerializedFileList(String strSerialPath)
    {
    	HashMap<String, Integer> strReturnList = new HashMap<String, Integer>();
    	
    	File stSerialFile = new File(strSerialPath);
    	do
    	{
    		if(!stSerialFile.exists())
    		{
    			break;
    		}
    		else if(!stSerialFile.canRead())
    		{
    			break;
    		}
    		else if(stSerialFile.isFile())
    		{
    			try {
					Scanner stFIn = new Scanner(stSerialFile);
					
					while(stFIn.hasNextLine())
					{
						String strPath = stFIn.nextLine();
						if(false == strReturnList.containsKey(strPath))
						{
							strReturnList.put(strPath, 1);						
						}
							
					}
					
					stFIn.close();
					
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	} while(false);
    	
    	return strReturnList;
    }
    
    private void serializeFileList(HashMap<String, Integer> stFileList)
    {
		try {
	        FileOutputStream output;
			output = new FileOutputStream(m_strSerializeDirectory);
			
	    	for(String s : stFileList.keySet())
	    	{
	    		output.write(s.getBytes());
	    	}

			output.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
  public static void main(String[] args) throws IOException, ParseException {
    // 0. Specify the analyzer for tokenizing text.
    //    The same analyzer should be used for indexing and searching
    StandardAnalyzer analyzer = new StandardAnalyzer();

    // 1. create the index
    File stIndexFileLocation = new File(m_strIndexDirectory);
    Directory index = FSDirectory.open(stIndexFileLocation.toPath());

    IndexWriterConfig config = new IndexWriterConfig(analyzer);
    config.setOpenMode(OpenMode.CREATE_OR_APPEND);

    IndexWriter w = new IndexWriter(index, config);
//    addDoc(w, "Lucene in Action", "193398817");
//    addDoc(w, "Lucene for Dummies", "55320055Z");
//    addDoc(w, "Managing Gigabytes", "55063554A");
//    addDoc(w, "The Art of Computer Science", "9900333X");
    TestLucene t = new TestLucene();
    HashMap<String, Integer> stSerializedFileList = t.getSerializedFileList(m_strIndexDirectory);
    ArrayList<String> stFileList = t.getFileList(m_strDataDirectory);
    for(String s : stFileList)
    {
    	try {
    		if(false == stSerializedFileList.containsKey(s))
    		{
    			w.addDocument(t.getDocument(s));
    			stSerializedFileList.put(s, 1);
    		}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    w.close();

    String strQuery = "";
    Scanner in = new Scanner(System.in);
    do
    {
    	System.out.print("Please type text : ");
    	strQuery = in.nextLine();
    	if(0 == strQuery.compareTo("exit"))
    	{
    		break;
    	}
        // 2. query

        // the "title" arg specifies the default field to use
        // when no field is explicitly specified in the query.
    	Query q = new QueryParser("contents", analyzer).parse(strQuery);

        // 3. search
        int hitsPerPage = 10;
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
        searcher.search(q, collector);
        ScoreDoc[] hits = collector.topDocs().scoreDocs;
       
        // 4. display results
        System.out.println("Found " + hits.length + " hits.");
        for(int i=0;i<hits.length;++i) {
          int docId = hits[i].doc;
          Document d = searcher.doc(docId);
          System.out.println((i + 1) + ". " + d.get("filename"));
        }

        // reader can only be closed when there
        // is no need to access the documents any more.
        reader.close();    	
    } while("exit" != strQuery);
    in.close();
  }
}