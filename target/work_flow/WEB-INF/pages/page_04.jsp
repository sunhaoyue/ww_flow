<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title></title>
<link href="../css/csspage_01.css" rel="stylesheet" type="text/css" />
<link href="../css/csspage_03.css" rel="stylesheet" type="text/css" />
</head>

<body>
 <div class="rightpage01">
  <div class="headbg01">
    <table width="100" border="0" cellspacing="0" cellpadding="0">
      <tr>
        <td width="35"><img src="../images/ico09.gif" width="33" height="27" /></td>
        <td><a href="#">确定</a></td>
      </tr>
    </table>
   </div>
  <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr>
      <td><table width="400" border="0" cellspacing="8" cellpadding="0">
        <tr>
          <td width="100" align="right">文件状态：</td>
          <td><select name="select" id="select">
              <option>所有</option>
            </select>
          </td>
        </tr>
        <tr>
          <td align="right"><input type="radio" name="radio" id="radio" value="radio" />
            我提交的文件</td>
          <td><input type="radio" name="radio2" id="radio2" value="radio2" />
            我审核的文件</td>
        </tr>
        <tr>
          <td align="right">提交日期:</td>
          <td><select name="select2" id="select2">
              <option>2012.6.6</option>
            </select>
            &nbsp;&nbsp;至&nbsp;&nbsp;
            <select name="select3" id="select3">
              <option>2012.6.6</option>
            </select></td>
        </tr>
        <tr>
          <td align="right">文件标题:</td>
          <td><input type="text" name="textfield" id="textfield" /></td>
        </tr>
        <tr>
          <td align="right">文件编号:</td>
          <td><input type="text" name="textfield2" id="textfield2" /></td>
        </tr>
        <tr>
          <td align="right">提交人:</td>
          <td><input type="text" name="textfield3" id="textfield3" />
            &nbsp;&nbsp;<a href="#" class="text04">选择</a>&nbsp;&nbsp;&nbsp;&nbsp;<a href="#" class="text04">清空</a></td>
        </tr>
        <tr>
          <td align="right">申请人姓名:</td>
          <td><input type="text" name="textfield4" id="textfield4" />
&nbsp;&nbsp;<a href="#" class="text04">选择</a>&nbsp;&nbsp;&nbsp;&nbsp;<a href="#" class="text04">清空</a></td>
        </tr>
      </table></td>
    </tr>
  </table>
</div>
</body>
</html>
