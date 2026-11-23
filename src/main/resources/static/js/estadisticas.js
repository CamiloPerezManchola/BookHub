
document.addEventListener("DOMContentLoaded", function () {

    const canvas = document.getElementById("genresChart");

    if (!canvas) {
        return;
    }

    const labels = window.bookHubGenreLabels || [];
    const values = window.bookHubGenreValues || [];

    if (labels.length === 0) {
        return;
    }

    // Get translated text from Thymeleaf
    const translations = document.getElementById(
        "statisticsTranslations"
    );

    const completedBooksLabel =
        translations.dataset.completedBooks;

    const booksCompletedLabel =
        translations.dataset.booksCompleted;

    new Chart(canvas, {
        type: "bar",

        data: {
            labels: labels,

            datasets: [{
                label: completedBooksLabel,

                data: values,

                backgroundColor: [
                    "#6385B5",
                    "#8A78BD",
                    "#6AA99B",
                    "#D5A46C",
                    "#C77C91",
                    "#7A9BB5"
                ],

                borderRadius: 8,

                borderSkipped: false,

                maxBarThickness: 55
            }]
        },

        options: {
            responsive: true,

            maintainAspectRatio: false,

            plugins: {
                legend: {
                    display: false
                },

                tooltip: {
                    callbacks: {
                        label: function (context) {
                            return context.parsed.y +
                                " " +
                                booksCompletedLabel;
                        }
                    }
                }
            },

            scales: {
                y: {
                    beginAtZero: true,

                    ticks: {
                        precision: 0
                    },

                    grid: {
                        color: "#edf0f3"
                    }
                },

                x: {
                    grid: {
                        display: false
                    }
                }
            }
        }
    });

});
