package com.esenlermotionstar.nogate.ContentApproval;




public class ApprovedContent {

    public static abstract class TitleChangeEvent
    {
        public abstract void titleIsChanged();
    }

    public static final String saveFile = "contentlist.xml",
            TYPE_ATTR_YOUTUBE = "youtube", TYPE_ATTR_FILE = "file",
            CONTENT_TYPE = "type", CONTENT_PATH_ATTR_NAME = "path",CONTENT_TAG = "content";

    private String dataPath ="" ;
    public void setDataPath(String dataPath) {
        /* Youtube videousu ise sadece video kodu ('watch?v=' sonrası), dosya ise tam yolu */
        this.dataPath = dataPath;
    }
    public String getDataPath() {
        /* Youtube videousu ise sadece video kodu ('watch?v=' sonrası), dosya ise tam yolu */
        return dataPath;
    }
    private String title = "";

    public void setTitle(String title) {
        this.title = title;
      callTitleChangeEvent();
    }

    public String getTitle() {
        return title;

    }
    private TitleChangeEvent titleChangeEvent;

    public void setTitleChangeEvent(TitleChangeEvent titleChangeEvent) {
        this.titleChangeEvent = titleChangeEvent;
    }


    private String type = "";
    public void setType(String type) {

        this.type = type;

    }

    private void callTitleChangeEvent() {
        if (titleChangeEvent != null) titleChangeEvent.titleIsChanged();
    }

    public String getType() {
        return type;
    }
    public boolean isTypeThat(String stype) {

        return type.equalsIgnoreCase(stype);
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ApprovedContent)
        {
            ApprovedContent obj_ = (ApprovedContent)obj;
            return (obj_.type.equals(this.type) && obj_.title.equals(this.title) && obj_.dataPath.equals(this.dataPath));
        } else
        return super.equals(obj);
    }


    @Override
    public String toString() {
        return super.toString() + " Type: " + this.getType() + ", Title: " + this.getTitle();
    }
}

