package util

  class XmlUtil() {
  
 
  def xmlEscapeText(t: String): String ={
  if (t == null || t.length == 0) return ""
	var sb = new StringBuilder()
	var i = 0
	var c = ' '
	for(i <- 0 to t.length-1){
		c = t(i);
		if (c == '<') sb.append("&lt;")
		else if (c == '<') sb.append("&lt;")
		else if (c == '>') sb.append("&gt;")
		else if (c == '\"') sb.append("&quot;")
		else if (c == '&') sb.append("&amp;")
		else if (c == '\'') sb.append("&apos;")
		else {
				if (c>0x7e) {
					sb.append("&#"+c.toInt+";")
				} else {
					sb.append(c)
				}
		}
	}
	return sb.toString();
  }

}
  