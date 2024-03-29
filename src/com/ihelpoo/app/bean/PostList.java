/*
 * Copyright (c) 2013 Wobang Network.
 *
 * Licensed under the GNU General Public License, version 2 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/gpl-2.0.html
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.ihelpoo.app.bean;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.ihelpoo.app.AppException;
import com.ihelpoo.app.common.StringUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

/**
 * 帖子列表实体类
 * @version 1.0
 * @created 2012-3-21
 */
public class PostList extends Entity{

	public final static int CATALOG_ASK = 1;
	public final static int CATALOG_SHARE = 2;
	public final static int CATALOG_OTHER = 3;
	public final static int CATALOG_JOB = 4;
	public final static int CATALOG_SITE = 5;
	
	private int pageSize;
	private int postCount;
	private List<Post> postlist = new ArrayList<Post>();
	
	public int getPageSize() {
		return pageSize;
	}
	public int getPostCount() {
		return postCount;
	}
	public List<Post> getPostlist() {
		return postlist;
	}

	public static PostList parse(InputStream inputStream) throws IOException, AppException {
		PostList postlist = new PostList();
		Post post = null;
        //获得XmlPullParser解析器
        XmlPullParser xmlParser = Xml.newPullParser();
        try {        	
            xmlParser.setInput(inputStream, UTF8);
            //获得解析到的事件类别，这里有开始文档，结束文档，开始标签，结束标签，文本等等事件。
            int evtType=xmlParser.getEventType();
			//一直循环，直到文档结束    
			while(evtType!=XmlPullParser.END_DOCUMENT){ 
	    		String tag = xmlParser.getName(); 
			    switch(evtType){ 
			    	case XmlPullParser.START_TAG:			    		
			    		if(tag.equalsIgnoreCase("post_count"))
			    		{
			    			postlist.postCount = StringUtils.toInt(xmlParser.nextText(), 0);
			    		}
			    		else if(tag.equalsIgnoreCase("page_size"))
			    		{
			    			postlist.pageSize = StringUtils.toInt(xmlParser.nextText(),0);
			    		}
			    		else if (tag.equalsIgnoreCase(Post.NODE_START)) 
			    		{ 
			    			post = new Post();
			    		}
			    		else if(post != null)
			    		{	
				            if(tag.equalsIgnoreCase(Post.NODE_ID))
				            {			      
				            	post.id = StringUtils.toInt(xmlParser.nextText(),0);
				            }
				            else if(tag.equalsIgnoreCase(Post.NODE_TITLE))
				            {			            	
				            	post.setTitle(xmlParser.nextText());
				            }
				            else if(tag.equalsIgnoreCase(Post.NODE_FACE))
				            {			            	
				            	post.setFace(xmlParser.nextText());
				            }
				            else if(tag.equalsIgnoreCase(Post.NODE_AUTHOR))
				            {			            	
				            	post.setAuthor(xmlParser.nextText());		            	
				            }
				            else if(tag.equalsIgnoreCase(Post.NODE_AUTHORID))
				            {			            	
				            	post.setAuthorId(StringUtils.toInt(xmlParser.nextText(),0));		            	
				            }
				            else if(tag.equalsIgnoreCase(Post.NODE_ANSWERCOUNT))
				            {			            	
				            	post.setAnswerCount(StringUtils.toInt(xmlParser.nextText(),0));			            	
				            }
				            else if(tag.equalsIgnoreCase(Post.NODE_VIEWCOUNT))
				            {			            	
				            	post.setViewCount(StringUtils.toInt(xmlParser.nextText(),0));			            	
				            }
				            else if(tag.equalsIgnoreCase(Post.NODE_PUBDATE))
				            {			            	
				            	post.setPubDate(xmlParser.nextText());         	
				            }
                            else if(tag.equalsIgnoreCase(Post.NODE_SAYTYPE))
                            {
                                post.setSayType(xmlParser.nextText());
                            }
			    		}
			            //通知信息
			            else if(tag.equalsIgnoreCase("notice"))
			    		{
			            	postlist.setNotice(new Notice());
			    		}
			            else if(postlist.getNotice() != null)
			    		{
                            if(tag.equalsIgnoreCase("systemCount"))
                            {
                                postlist.getNotice().setSystemCount(StringUtils.toInt(xmlParser.nextText(), 0));
                            }
                            else if(tag.equalsIgnoreCase("atmeCount"))
                            {
                                postlist.getNotice().setAtmeCount(StringUtils.toInt(xmlParser.nextText(),0));
                            }
                            else if(tag.equalsIgnoreCase("commentCount"))
                            {
                                postlist.getNotice().setCommentCount(StringUtils.toInt(xmlParser.nextText(),0));
                            }
                            else if(tag.equalsIgnoreCase("activeCount"))
                            {
                                postlist.getNotice().setActiveCount(StringUtils.toInt(xmlParser.nextText(),0));
                            }
                            else if(tag.equalsIgnoreCase("chatCount"))
                            {
                                postlist.getNotice().setChatCount(StringUtils.toInt(xmlParser.nextText(),0));
                            }
			    		}
			    		break;
			    	case XmlPullParser.END_TAG:	
					   	//如果遇到标签结束，则把对象添加进集合中
				       	if (tag.equalsIgnoreCase("post") && post != null) { 
				    	   postlist.getPostlist().add(post); 
				           post = null; 
				       	}
				       	break; 
			    }
			    //如果xml没有结束，则导航到下一个节点
			    evtType=xmlParser.next();
			}		
        } catch (XmlPullParserException e) {
			throw AppException.xml(e);
        } finally {
        	inputStream.close();	
        }      
        return postlist;       
	}
}
