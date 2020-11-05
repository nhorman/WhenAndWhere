
function initPage() {
    var answers = document.getElementsByClassName("ANSWER");
    console.log(answers);
    for (const a of answers) {
        console.log(a);
        storycontrol.registerAnswer(a.value);
    }

}
