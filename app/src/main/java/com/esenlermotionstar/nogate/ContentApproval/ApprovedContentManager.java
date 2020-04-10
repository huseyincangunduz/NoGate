package com.esenlermotionstar.nogate.ContentApproval;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.esenlermotionstar.nogate.SystemControllers.NetworkController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.ArrayList;

public class ApprovedContentManager {
    //KONSTANTLAR
    public static final String saveFile = "contentlist.xml",
            TYPE_ATTR_YOUTUBE = "youtube",
            TYPE_ATTR_FILE = "file",
            CONTENT_TYPE = "type",
            CONTENT_PATH_ATTR_NAME = "path", CONTENT_TITLE = "title",
            CONTENT_TAG = "content", PREFERENCE_DEFAULT_NAME = "NoGatePrefs";


    //STATİK FONKSİYONLAR
    protected static ArrayList<ApprovedContent> readyList,
            iterationFullList, iterationYTList, iterationLocalList;
    private static Context AppContext;

    protected static void addToIterationList(ApprovedContent content) {
        if (content.isTypeThat(ApprovedContent.TYPE_ATTR_YOUTUBE)) iterationYTList.add(content);
        else if (content.isTypeThat(ApprovedContent.TYPE_ATTR_FILE)) iterationLocalList.add(content);
    }

    /*İterasyon listelerini kayıt vs için değil de, özel network durumları için kilit ekranında
    gösterilenleri ayarlamak için. Eğer Hücreselde 3-4 niloya ya da Gamze Gamze videosu açtığını düşünürsek bayağı sıkıntı olacak.
    network ve ya vb. durumlar için otomatik ayarlamam gerekiyor.*/
    private static ApprovedContentManager instanceOfThis = null;

    public static int ReadyListLength() {
        return readyList.size();
    }

    static int sira = -1;

    public static int getSira() {
        Log.i("İterasyon sırası: ", String.valueOf(sira));
        return sira;
    }

    public static void setSira(int sira_) {
        Log.i("İterasyon sırası: ", String.valueOf(sira_));
        ApprovedContentManager.sira = sira_;
        SharedPreferences preferences = AppContext.getSharedPreferences(PREFERENCE_DEFAULT_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("sira", sira_ > -1 ? sira_ : 0);
        // editor.commit();
        editor.apply();
    }

    public static ApprovedContent iterate() {
        if (readyList.size() > 0) {
            setSira((sira + 1) % readyList.size());
            Log.i("Iterate", String.format("Iterated Approved Content: %d item", getSira()));
            return readyList.get(sira);
        }
        return null;
    }

    public static ApprovedContentManager getInstance(Context ctx) {
        AppContext = ctx;
        if (instanceOfThis == null) {
            instanceOfThis = new ApprovedContentManager();
            SharedPreferences preferences = AppContext.getSharedPreferences(PREFERENCE_DEFAULT_NAME, Context.MODE_PRIVATE);
            if (preferences.contains("sira")) {
                sira = preferences.getInt("sira", -1);
            }
        }
        return instanceOfThis;
    }

    public static void setIterationListByNetwork(NetworkController.ConnectionMode connectionMode) {
        if (AppContext != null)
        {
            boolean cellular = connectionMode.equals(NetworkController.ConnectionMode.Cellular);
            boolean accept_in_cellular = PreferenceManager
                    .getDefaultSharedPreferences(AppContext)
                    .getBoolean("youtube_watchable_in_cellular", false);
            Log.i(null, String.valueOf(accept_in_cellular));

            if (connectionMode == NetworkController.ConnectionMode.Wireless || (cellular && accept_in_cellular)) {
                readyList = iterationFullList;
                Log.i(null, "İterasyon: Tam listeye geçildi");
            } else {
                readyList = iterationLocalList;
                Log.i(null, "İterasyon: Çevrimdışı listeye geçildi");
            }

            Log.i(null, String.format("Bağlantı Modu: %s", connectionMode.toString()));
        }



    }


    private static Boolean initialized = false;

    public static Boolean getInitialized() {
        return initialized;
    }

    public static ArrayList<ApprovedContent> getFullList() {
        return iterationFullList;
    }

    public static ApprovedContent lastApprovedContent() {
        if (readyList.size() > 0) {

            Log.i("Iterate", String.format("Iterated Approved Content: %d item", getSira()));
            return readyList.get(sira);
        }
        return null;
    }

    public static void removeItemByIndex(int i) {
        ApprovedContent target = getFullList().get(i);
        if (target != null) {
            iterationFullList.remove(i);

            if (target.isTypeThat(TYPE_ATTR_YOUTUBE)) {
                iterationYTList.remove(target);
            } else if (target.isTypeThat(TYPE_ATTR_FILE)) {
                iterationLocalList.remove(target);
            }
        }

    }

    public static void insertApprovedContent(ApprovedContent content) {
        getFullList().add(content);
        addToIterationList(content);
    }

    public static void decreaseSira() {
        int s = getSira() - 1;

        setSira(s >= 0 ? s : 0);
    }

    //SINIF FONKSİYONLARI
    public ArrayList<ApprovedContent> getApprovedContents() {
        return approvedContents;
    }

    String dataDir, saveFileFullPath;
    ArrayList<ApprovedContent> approvedContents; //BU KAYIT İÇİN KULLANILACAK

    protected ApprovedContentManager() {
        readApprovedContentsFromXML();
    }

    public void readApprovedContentsFromXML() {
        if (!initialized) {
            try {


                iterationYTList = new ArrayList<>();
                iterationLocalList = new ArrayList<>();

                dataDir = "/data/data/com.esenlermotionstar.nogate";
                saveFileFullPath = dataDir + "/" + saveFile;
                File fil = new File(saveFileFullPath);
                approvedContents = new ArrayList<>();
                iterationFullList = approvedContents;
                if (fil.exists()) {


                    DocumentBuilderFactory docBuildFact = DocumentBuilderFactory.newInstance();
                    DocumentBuilder docBuilder = docBuildFact.newDocumentBuilder();
                    Document doc;
                    doc = docBuilder.parse(fil);
                    NodeList nodes = doc.getElementsByTagName(CONTENT_TAG);
                    for (int i = 0; i < nodes.getLength(); i++) {
                        Element currElement = (Element) nodes.item(i);
                        if (currElement.hasAttribute(CONTENT_PATH_ATTR_NAME) && currElement.hasAttribute(CONTENT_TYPE)) {
                            String contentTypeAttr = currElement.getAttribute(CONTENT_TYPE);
                            String contentPath = currElement.getAttribute(CONTENT_PATH_ATTR_NAME);
                            String contentTitle= currElement.getAttribute(CONTENT_TITLE);
                            //Onaylıİçerik sınıfı için xml elementlerinden özellikleri alıyorum
                            if (contentTypeAttr.equals(ApprovedContent.TYPE_ATTR_YOUTUBE) || (new File(contentPath)).exists()) {
                                ApprovedContent content = new ApprovedContent();
                                content.setType(contentTypeAttr);
                                content.setDataPath(contentPath);
                                content.setTitle(contentTitle);
                                //Onaylıİçerik sınıfının özelliklerini uyguluyoruz
                                approvedContents.add(content);
                                addToIterationList(content);
                            }

                        }
                    }


                } else {

                }


                readyList = iterationFullList = approvedContents;

                initialized = true;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }


    }

    void saveApprovedContentsFromXML(ArrayList<ApprovedContent> approvedContents_) {
        try {
            DocumentBuilderFactory docBuildFact = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuildFact.newDocumentBuilder();
            Document doc;
            doc = docBuilder.newDocument();
            Element rootEl = doc.createElement("approvedContents");
            for (int i = 0; i < approvedContents_.size(); i++) {
                ApprovedContent content = approvedContents_.get(i);

                Element contentXmlElement = doc.createElement(CONTENT_TAG);
                contentXmlElement.setAttribute(CONTENT_TYPE, content.getType());
                contentXmlElement.setAttribute(CONTENT_PATH_ATTR_NAME, content.getDataPath());
                contentXmlElement.setAttribute(CONTENT_TITLE,content.getTitle());
                rootEl.appendChild(contentXmlElement);

            }

            doc.appendChild(rootEl);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            Result output = new StreamResult(new File(saveFileFullPath));
            Source input = new DOMSource(doc);

            transformer.transform(input, output);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void saveApprovedContentsFromXML() {
        saveApprovedContentsFromXML(approvedContents);
    }
}
