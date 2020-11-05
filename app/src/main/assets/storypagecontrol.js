
function initPage() {
    var answers = document.getElementsByClassName("ANSWER");
    for (const a of answers) {
        console.log(a);
        var id = storycontrol.registerAnswer(a.value);
        a.id = id;
        a.size = storycontrol.getTrueAnswerSize(a.value);
        a.value = "";
        a.readOnly = true;
    }
}

function updateAnswers() {
    var answers = document.getElementsByClassName("ANSWER");
    for (const a of answers) {
        a.value = storycontrol.updateAnswerField(a.id);
    }
}
