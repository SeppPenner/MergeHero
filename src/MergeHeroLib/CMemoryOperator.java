/*
 * MergeHero: Differing and Merging Folders & Files
 *
 * Copyright © 2004, Dynamsoft, Inc. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.opensource.org/licenses/gpl-3.0.html.
 */

/*
 * Created on 2006-6-26
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package MergeHeroLib;

/**
 * @author Falcon Young
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CMemoryOperator 
{
	    public static void main(String[] args)
	    {
	        byte[] buf1 = {'0','1','3','4','5','6','7','8'};
	        byte[] buf2 = {'0','1','3','4','5','6','7','8', '9'};
	        byte[] buf3 = {'0','1','4','4','5','6','7','8', '9'};
	        
	        int result = CMemoryOperator.memcmp(buf1, buf2, 8);
	        System.out.println(result);
	        result = CMemoryOperator.memcmp(buf1, buf3, 3);
	        System.out.println(result);
	        result = CMemoryOperator.memcmp(buf2, buf3, 8);
	        System.out.println(result);
	    }
	    
	    public static int memcmp(byte[] buf1, byte[] buf2, int size)
	    {
	        int Size1 = buf1.length;
	        int Size2 = buf2.length;
	        int comSize = Size1 < Size2 ? Size1 : Size2;
	        int result = 0;
	        
	        for (int i = 0; i < comSize; i++)
	        {
	            if (i == size)
	            {
	                return result;
	            }
	            else
	            {
	                result = buf1[i] - buf2[i];
	                if (result == 0)
	                {
	                    continue;
	                }
	                else
	                {
	                    return result;
	                }
	            }
	        }
	        
	        if (Size1 > Size2)
	        {
	            return 1;
	        }
	        else if (Size1 < Size2)
	        {
	            return -1;
	        }
	        else
	        {
	            return result;
	        }
	     }
	    
	    public static void CopyMemory(byte[] destBuf, byte[] orgBuf, int size)
	    {
	        int orgSize = orgBuf.length;
	        int destSize = destBuf.length;
	        int copySize = orgSize < destSize ? orgSize : destSize;
	        
	        for (int i = 0; i < copySize; i++)
	        {
	            if (i == size)
	            {
	                break;
	            }
	            
	            destBuf[i] = orgBuf[i];
	        }
	    }
	    
	    public static void CopyMemory(char[] destBuf, char[] orgBuf, int size)
	    {
	        int orgSize = orgBuf.length;
	        int destSize = destBuf.length;
	        int copySize = orgSize < destSize ? orgSize : destSize;
	        
	        for (int i = 0; i < copySize; i++)
	        {
	            if (i == size)
	            {
	                break;
	            }
	            
	            destBuf[i] = orgBuf[i];
	        }
	    }
}
