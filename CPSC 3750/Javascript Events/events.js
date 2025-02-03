//Counters for each button click and hover
let redCount = 0;
let greenCount = 0;
let blueCount = 0;
let hoverRedCount = 0;
let hoverGreenCount = 0;
let hoverBlueCount = 0;

//get button elements
const redBtn = document.getElementById("redbtn");
const greenBtn = document.getElementById("greenbtn");
const blueBtn = document.getElementById("bluebtn");

// Get counter elements
const redCounter = document.getElementById("redCounter");
const greenCounter = document.getElementById("greenCounter");
const blueCounter = document.getElementById("blueCounter");

const redHover = document.getElementById("redHover");
const greenHover = document.getElementById("greenHover");
const blueHover = document.getElementById("blueHover");

// Event listeners for click events
redBtn.addEventListener("click", function() {
    document.body.style.backgroundColor = "red";
    redCount++;
    redCounter.textContent = `RED count: ${redCount}`;
});

greenBtn.addEventListener("click", function() {
    document.body.style.backgroundColor = "green";
    greenCount++;
    greenCounter.textContent = `GREEN count: ${greenCount}`;
});

blueBtn.addEventListener("click", function() {
    document.body.style.backgroundColor = "blue";
    blueCount++;
    blueCounter.textContent = `BLUE count: ${blueCount}`;
});

// Function to handle hover effects with counter updates
function addHoverEffect(button, color, counterVar, counterElement) {
    button.addEventListener("mouseenter", function() {
        button.style.backgroundColor = "white";
        button.style.color = "black";

        // Increment and update the global counter correctly
        if (color === "red") hoverRedCount++;
        if (color === "green") hoverGreenCount++;
        if (color === "blue") hoverBlueCount++;

        // Update the correct hover counter text
        counterElement.textContent = `Hover ${color.toUpperCase()} count: ${color === "red" ? hoverRedCount : color === "green" ? hoverGreenCount : hoverBlueCount}`;
    });

    button.addEventListener("mouseleave", function() {
        button.style.backgroundColor = "black";
        button.style.color = "white";
    });
}

// Apply hover effects and track counters with the correct IDs
addHoverEffect(redBtn, "red", hoverRedCount, redHover);
addHoverEffect(greenBtn, "green", hoverGreenCount, greenHover);
addHoverEffect(blueBtn, "blue", hoverBlueCount, blueHover);