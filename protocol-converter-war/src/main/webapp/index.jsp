<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Конвертер протоколов</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css">
</head>
<body>
<div class="container">
    <h1>XML сюда</h1>
    <form role="form" method="POST" action="/qwerty">
        <div class="form-group">
            <textarea name="xml"></textarea>
        </div>
        <input type="submit" class="btn btn-default" name="parse">
    </form>
</div>
</body>
</html>
