package com.newsbank.permuter.types;

public class Config {
	
	String fulltext= "OCR PAGETEXT";
	String citation= "SART LED ABS MNT SBODY DECE HED SHL SRI COL KIK DEK U130 U240 U245 U250 U730 U740 ART_TITLE TITLE1 TITLE2 TITLE3 CAP GRC CAPTION U300 U755 ASPPD MEM SEC PAG_HEADER EDT ASPVLN PAG PAG_LABEL SBJ SUB CON SCBD SUBHED ART_SUBHEADING PNA MAUT LAST_NAME FIRST_NAME GEO UNFM U433 U433AD U433PD BNUM BTYPE SESS SESSA SESSC U600 U610 U611 U630 U650 U651 U695A U695B EVNT EXDN EXDO NRRW SGSCAT SGSTERM GNA IGR CAT COM ORG KEY NAM TOP TYP IND TGE TKY TPE TOR METADATA ENV PRP U500 U505 U510 U655 U700 U710 U711 U749 U753 PCAT PCLASS MHED MGEO MDES WTNS SUDOC STATLG LANG PNUM SSVL ASPVL SVOL SVCL CNOT MNUM MSCALE MCOORD SSNO ASPNO APNUM DEPTNAM TERNAM TERNO TERPD PARKERTITLE NARACOLL NARAGRPTITLE OPB U745 SRCNAME ART_SOURCE_NAME U752 PLOC DOC_PLOC AUT BLC SRC WSR U100 U110 U111 CBOD CCBD CSTC CMN OFPR EXOA EXDA ART_AUTHOR";
	String alltext= "SART OCR PAGETEXT LED ABS MNT SBODY DECE HED SHL SRI COL KIK DEK U130 U240 U245 U250 U730 U740 ART_TITLE TITLE1 TITLE2 TITLE3 CAP GRC CAPTION U300 U755 ASPPD MEM SEC PAG_HEADER EDT ASPVLN PAG PAG_LABEL SBJ SUB CON SCBD SUBHED ART_SUBHEADING PNA MAUT LAST_NAME FIRST_NAME GEO UNFM U433 U433AD U433PD BNUM BTYPE SESS SESSA SESSC U600 U610 U611 U630 U650 U651 U695A U695B EVNT EXDN EXDO NRRW SGSCAT SGSTERM GNA IGR CAT COM ORG KEY NAM TOP TYP IND TGE TKY TPE TOR METADATA ENV PRP U500 U505 U510 U655 U700 U710 U711 U749 U753 PCAT PCLASS MHED MGEO MDES WTNS SUDOC STATLG LANG PNUM SSVL ASPVL SVOL SVCL CNOT MNUM MSCALE MCOORD SSNO ASPNO APNUM DEPTNAM TERNAM TERNO TERPD PARKERTITLE NARACOLL NARAGRPTITLE OPB U745 SRCNAME ART_SOURCE_NAME U752 PLOC DOC_PLOC AUT BLC SRC WSR U100 U110 U111 CBOD CCBD CSTC CMN OFPR EXOA EXDA ART_AUTHOR";

	public Config() {
	}
	
	public Config(String fulltext, String citation, String alltext, String elmWithin) {
		super();
		this.fulltext = fulltext;
		this.citation = citation;
		this.alltext = alltext;
	}
	
	public String getFulltext() {
		return fulltext;
	}
	public void setFulltext(String fulltext) {
		this.fulltext = fulltext;
	}
	public String getCitation() {
		return citation;
	}
	public void setCitation(String citation) {
		this.citation = citation;
	}
	public String getAlltext() {
		return alltext;
	}
	public void setAlltext(String alltext) {
		this.alltext = alltext;
	}
}
