require(
    [
        'requireLib',
        'jquery'
    ],
    function(require, $){
        $('.placeholder-input').live("keyup",function(e){
            if($(this).val() !== "") $(this).addClass("on");
            else $(this).removeClass("on")
        });
        $(document).ready(function(){
            $('.placeholder-input').each(function(i){
                if($(this).val() !== "") $(this).addClass("on");
                else $(this).removeClass("on")
            });
        });
    }
);
