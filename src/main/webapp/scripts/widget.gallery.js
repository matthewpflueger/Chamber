require(
    [
        'requireLib',
        'jquery',
        'components/widget/gallery'
    ],
    function(requireLib, jquery, Gallery){

        $(document).ready(function(){
            var properties = Echoed;
            var gallery = new Gallery({ properties: properties})
        });
  }
);