package fun.utils.api.core.common;

import org.apache.commons.collections.map.HashedMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApiConst {
    public final static List<String> PUBLIC_GROOVY_IMPORTS = new ArrayList<>();
    public final static Map<String,String> FILE_MIME_TYPES = new HashedMap();

    static {

        PUBLIC_GROOVY_IMPORTS.add("import com.alibaba.fastjson.*;");
        PUBLIC_GROOVY_IMPORTS.add("org.apache.commons.lang3.StringUtils;");

        Map<String,String> m = FILE_MIME_TYPES;
        m.put(".*","application/octet-stream");
        m.put("tif","image/tiff");
        m.put("001","application/x-001");
        m.put("301","application/x-301");
        m.put("323","text/h323");
        m.put("906","application/x-906");
        m.put("907","drawing/907");
        m.put("a11","application/x-a11");
        m.put("acp","audio/x-mei-aac");
        m.put("ai","application/postscript");
        m.put("aif","audio/aiff");
        m.put("aifc","audio/aiff");
        m.put("aiff","audio/aiff");
        m.put("anv","application/x-anv");
        m.put("asa","text/asa");
        m.put("asf","video/x-ms-asf");
        m.put("asp","text/asp");
        m.put("asx","video/x-ms-asf");
        m.put("au","audio/basic");
        m.put("avi","video/avi");
        m.put("awf","application/vnd.adobe.workflow");
        m.put("biz","text/xml");
        m.put("bmp","application/x-bmp");
        m.put("bot","application/x-bot");
        m.put("c4t","application/x-c4t");
        m.put("c90","application/x-c90");
        m.put("cal","application/x-cals");
        m.put("cat","application/vnd.ms-pki.seccat");
        m.put("cdf","application/x-netcdf");
        m.put("cdr","application/x-cdr");
        m.put("cel","application/x-cel");
        m.put("cer","application/x-x509-ca-cert");
        m.put("cg4","application/x-g4");
        m.put("cgm","application/x-cgm");
        m.put("cit","application/x-cit");
        m.put("class","java/*");
        m.put("cml","text/xml");
        m.put("cmp","application/x-cmp");
        m.put("cmx","application/x-cmx");
        m.put("cot","application/x-cot");
        m.put("crl","application/pkix-crl");
        m.put("crt","application/x-x509-ca-cert");
        m.put("csi","application/x-csi");
        m.put("css","text/css");
        m.put("cut","application/x-cut");
        m.put("dbf","application/x-dbf");
        m.put("dbm","application/x-dbm");
        m.put("dbx","application/x-dbx");
        m.put("dcd","text/xml");
        m.put("dcx","application/x-dcx");
        m.put("der","application/x-x509-ca-cert");
        m.put("dgn","application/x-dgn");
        m.put("dib","application/x-dib");
        m.put("dll","application/x-msdownload");
        m.put("doc","application/msword");
        m.put("dot","application/msword");
        m.put("drw","application/x-drw");
        m.put("dtd","text/xml");
        m.put("dwf","Model/vnd.dwf");
        m.put("dwf","application/x-dwf");
        m.put("dwg","application/x-dwg");
        m.put("dxb","application/x-dxb");
        m.put("dxf","application/x-dxf");
        m.put("edn","application/vnd.adobe.edn");
        m.put("emf","application/x-emf");
        m.put("eml","message/rfc822");
        m.put("ent","text/xml");
        m.put("epi","application/x-epi");
        m.put("eps","application/x-ps");
        m.put("eps","application/postscript");
        m.put("etd","application/x-ebx");
        m.put("exe","application/x-msdownload");
        m.put("fax","image/fax");
        m.put("fdf","application/vnd.fdf");
        m.put("fif","application/fractals");
        m.put("fo","text/xml");
        m.put("frm","application/x-frm");
        m.put("g4","application/x-g4");
        m.put("gbr","application/x-gbr");
        m.put("","application/x-");
        m.put("gif","image/gif");
        m.put("gl2","application/x-gl2");
        m.put("gp4","application/x-gp4");
        m.put("hgl","application/x-hgl");
        m.put("hmr","application/x-hmr");
        m.put("hpg","application/x-hpgl");
        m.put("hpl","application/x-hpl");
        m.put("hqx","application/mac-binhex40");
        m.put("hrf","application/x-hrf");
        m.put("hta","application/hta");
        m.put("htc","text/x-component");
        m.put("htm","text/html");
        m.put("html","text/html");
        m.put("htt","text/webviewhtml");
        m.put("htx","text/html");
        m.put("icb","application/x-icb");
        m.put("ico","image/x-icon");
        m.put("ico","application/x-ico");
        m.put("iff","application/x-iff");
        m.put("ig4","application/x-g4");
        m.put("igs","application/x-igs");
        m.put("iii","application/x-iphone");
        m.put("img","application/x-img");
        m.put("ins","application/x-internet-signup");
        m.put("isp","application/x-internet-signup");
        m.put("IVF","video/x-ivf");
        m.put("java","java/*");
        m.put("jfif","image/jpeg");
        m.put("jpe","image/jpeg");
        m.put("jpe","application/x-jpe");
        m.put("jpeg","image/jpeg");
        m.put("jpg","image/jpeg");
        m.put("jpg","application/x-jpg");
        m.put("js","application/x-javascript");
        m.put("jsp","text/html");
        m.put("la1","audio/x-liquid-file");
        m.put("lar","application/x-laplayer-reg");
        m.put("latex","application/x-latex");
        m.put("lavs","audio/x-liquid-secure");
        m.put("lbm","application/x-lbm");
        m.put("lmsff","audio/x-la-lms");
        m.put("ls","application/x-javascript");
        m.put("ltr","application/x-ltr");
        m.put("m1v","video/x-mpeg");
        m.put("m2v","video/x-mpeg");
        m.put("m3u","audio/mpegurl");
        m.put("m4e","video/mpeg4");
        m.put("mac","application/x-mac");
        m.put("man","application/x-troff-man");
        m.put("math","text/xml");
        m.put("mdb","application/msaccess");
        m.put("mdb","application/x-mdb");
        m.put("mfp","application/x-shockwave-flash");
        m.put("mht","message/rfc822");
        m.put("mhtml","message/rfc822");
        m.put("mi","application/x-mi");
        m.put("mid","audio/mid");
        m.put("midi","audio/mid");
        m.put("mil","application/x-mil");
        m.put("mml","text/xml");
        m.put("mnd","audio/x-musicnet-download");
        m.put("mns","audio/x-musicnet-stream");
        m.put("mocha","application/x-javascript");
        m.put("movie","video/x-sgi-movie");
        m.put("mp1","audio/mp1");
        m.put("mp2","audio/mp2");
        m.put("mp2v","video/mpeg");
        m.put("mp3","audio/mp3");
        m.put("mp4","video/mpeg4");
        m.put("mpa","video/x-mpg");
        m.put("mpd","application/vnd.ms-project");
        m.put("mpe","video/x-mpeg");
        m.put("mpeg","video/mpg");
        m.put("mpg","video/mpg");
        m.put("mpga","audio/rn-mpeg");
        m.put("mpp","application/vnd.ms-project");
        m.put("mps","video/x-mpeg");
        m.put("mpt","application/vnd.ms-project");
        m.put("mpv","video/mpg");
        m.put("mpv2","video/mpeg");
        m.put("mpw","application/vnd.ms-project");
        m.put("mpx","application/vnd.ms-project");
        m.put("mtx","text/xml");
        m.put("mxp","application/x-mmxp");
        m.put("net","image/pnetvue");
        m.put("nrf","application/x-nrf");
        m.put("nws","message/rfc822");
        m.put("odc","text/x-ms-odc");
        m.put("out","application/x-out");
        m.put("p10","application/pkcs10");
        m.put("p12","application/x-pkcs12");
        m.put("p7b","application/x-pkcs7-certificates");
        m.put("p7c","application/pkcs7-mime");
        m.put("p7m","application/pkcs7-mime");
        m.put("p7r","application/x-pkcs7-certreqresp");
        m.put("p7s","application/pkcs7-signature");
        m.put("pc5","application/x-pc5");
        m.put("pci","application/x-pci");
        m.put("pcl","application/x-pcl");
        m.put("pcx","application/x-pcx");
        m.put("pdf","application/pdf");
        m.put("pdf","application/pdf");
        m.put("pdx","application/vnd.adobe.pdx");
        m.put("pfx","application/x-pkcs12");
        m.put("pgl","application/x-pgl");
        m.put("pic","application/x-pic");
        m.put("pko","application/vnd.ms-pki.pko");
        m.put("pl","application/x-perl");
        m.put("plg","text/html");
        m.put("pls","audio/scpls");
        m.put("plt","application/x-plt");
        m.put("png","image/png");
        m.put("png","application/x-png");
        m.put("pot","application/vnd.ms-powerpoint");
        m.put("ppa","application/vnd.ms-powerpoint");
        m.put("ppm","application/x-ppm");
        m.put("pps","application/vnd.ms-powerpoint");
        m.put("ppt","application/vnd.ms-powerpoint");
        m.put("ppt","application/x-ppt");
        m.put("pr","application/x-pr");
        m.put("prf","application/pics-rules");
        m.put("prn","application/x-prn");
        m.put("prt","application/x-prt");
        m.put("ps","application/x-ps");
        m.put("ps","application/postscript");
        m.put("ptn","application/x-ptn");
        m.put("pwz","application/vnd.ms-powerpoint");
        m.put("r3t","text/vnd.rn-realtext3d");
        m.put("ra","audio/vnd.rn-realaudio");
        m.put("ram","audio/x-pn-realaudio");
        m.put("ras","application/x-ras");
        m.put("rat","application/rat-file");
        m.put("rdf","text/xml");
        m.put("rec","application/vnd.rn-recording");
        m.put("red","application/x-red");
        m.put("rgb","application/x-rgb");
        m.put("rjs","application/vnd.rn-realsystem-rjs");
        m.put("rjt","application/vnd.rn-realsystem-rjt");
        m.put("rlc","application/x-rlc");
        m.put("rle","application/x-rle");
        m.put("rm","application/vnd.rn-realmedia");
        m.put("rmf","application/vnd.adobe.rmf");
        m.put("rmi","audio/mid");
        m.put("rmj","application/vnd.rn-realsystem-rmj");
        m.put("rmm","audio/x-pn-realaudio");
        m.put("rmp","application/vnd.rn-rn_music_package");
        m.put("rms","application/vnd.rn-realmedia-secure");
        m.put("rmvb","application/vnd.rn-realmedia-vbr");
        m.put("rmx","application/vnd.rn-realsystem-rmx");
        m.put("rnx","application/vnd.rn-realplayer");
        m.put("rp","image/vnd.rn-realpix");
        m.put("rpm","audio/x-pn-realaudio-plugin");
        m.put("rsml","application/vnd.rn-rsml");
        m.put("rt","text/vnd.rn-realtext");
        m.put("rtf","application/msword");
        m.put("rtf","application/x-rtf");
        m.put("rv","video/vnd.rn-realvideo");
        m.put("sam","application/x-sam");
        m.put("sat","application/x-sat");
        m.put("sdp","application/sdp");
        m.put("sdw","application/x-sdw");
        m.put("sit","application/x-stuffit");
        m.put("slb","application/x-slb");
        m.put("sld","application/x-sld");
        m.put("slk","drawing/x-slk");
        m.put("smi","application/smil");
        m.put("smil","application/smil");
        m.put("smk","application/x-smk");
        m.put("snd","audio/basic");
        m.put("sol","text/plain");
        m.put("sor","text/plain");
        m.put("spc","application/x-pkcs7-certificates");
        m.put("spl","application/futuresplash");
        m.put("spp","text/xml");
        m.put("ssm","application/streamingmedia");
        m.put("sst","application/vnd.ms-pki.certstore");
        m.put("stl","application/vnd.ms-pki.stl");
        m.put("stm","text/html");
        m.put("sty","application/x-sty");
        m.put("svg","text/xml");
        m.put("swf","application/x-shockwave-flash");
        m.put("tdf","application/x-tdf");
        m.put("tg4","application/x-tg4");
        m.put("tga","application/x-tga");
        m.put("tif","image/tiff");
        m.put("tif","application/x-tif");
        m.put("tiff","image/tiff");
        m.put("tld","text/xml");
        m.put("top","drawing/x-top");
        m.put("torrent","application/x-bittorrent");
        m.put("tsd","text/xml");
        m.put("txt","text/plain");
        m.put("uin","application/x-icq");
        m.put("uls","text/iuls");
        m.put("vcf","text/x-vcard");
        m.put("vda","application/x-vda");
        m.put("vdx","application/vnd.visio");
        m.put("vml","text/xml");
        m.put("vpg","application/x-vpeg005");
        m.put("vsd","application/vnd.visio");
        m.put("vsd","application/x-vsd");
        m.put("vss","application/vnd.visio");
        m.put("vst","application/vnd.visio");
        m.put("vst","application/x-vst");
        m.put("vsw","application/vnd.visio");
        m.put("vsx","application/vnd.visio");
        m.put("vtx","application/vnd.visio");
        m.put("vxml","text/xml");
        m.put("wav","audio/wav");
        m.put("wax","audio/x-ms-wax");
        m.put("wb1","application/x-wb1");
        m.put("wb2","application/x-wb2");
        m.put("wb3","application/x-wb3");
        m.put("wbmp","image/vnd.wap.wbmp");
        m.put("wiz","application/msword");
        m.put("wk3","application/x-wk3");
        m.put("wk4","application/x-wk4");
        m.put("wkq","application/x-wkq");
        m.put("wks","application/x-wks");
        m.put("wm","video/x-ms-wm");
        m.put("wma","audio/x-ms-wma");
        m.put("wmd","application/x-ms-wmd");
        m.put("wmf","application/x-wmf");
        m.put("wml","text/vnd.wap.wml");
        m.put("wmv","video/x-ms-wmv");
        m.put("wmx","video/x-ms-wmx");
        m.put("wmz","application/x-ms-wmz");
        m.put("wp6","application/x-wp6");
        m.put("wpd","application/x-wpd");
        m.put("wpg","application/x-wpg");
        m.put("wpl","application/vnd.ms-wpl");
        m.put("wq1","application/x-wq1");
        m.put("wr1","application/x-wr1");
        m.put("wri","application/x-wri");
        m.put("wrk","application/x-wrk");
        m.put("ws","application/x-ws");
        m.put("ws2","application/x-ws");
        m.put("wsc","text/scriptlet");
        m.put("wsdl","text/xml");
        m.put("wvx","video/x-ms-wvx");
        m.put("xdp","application/vnd.adobe.xdp");
        m.put("xdr","text/xml");
        m.put("xfd","application/vnd.adobe.xfd");
        m.put("xfdf","application/vnd.adobe.xfdf");
        m.put("xhtml","text/html");
        m.put("xls","application/vnd.ms-excel");
        m.put("xls","application/x-xls");
        m.put("xlw","application/x-xlw");
        m.put("xml","text/xml");
        m.put("xpl","audio/scpls");
        m.put("xq","text/xml");
        m.put("xql","text/xml");
        m.put("xquery","text/xml");
        m.put("xsd","text/xml");
        m.put("xsl","text/xml");
        m.put("xslt","text/xml");
        m.put("xwd","application/x-xwd");
        m.put("x_b","application/x-x_b");
        m.put("sis","application/vnd.symbian.install");
        m.put("sisx","application/vnd.symbian.install");
        m.put("x_t","application/x-x_t");
        m.put("ipa","application/vnd.iphone");
        m.put("apk","application/vnd.android.package-archive");
        m.put("xap","application/x-silverlight-app");
    }



}
