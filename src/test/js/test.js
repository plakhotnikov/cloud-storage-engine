import http from 'k6/http';
import { check, sleep } from 'k6';

// Открываем файл в init-стадии (глобальный контекст)
const fileData = open('/Users/admin/Documents/cloud-storage-engine/src/test/js/008 Custom attribute converter.mp4', 'b');

export let options = {
    stages: [
        { duration: '10s', target: 50 },
        { duration: '30s', target: 100 },
        { duration: '10s', target: 0 }
    ],
};

export default function () {
    let url = 'http://localhost:8080/files/upload';

    let formData = {
        directoryId: '200', // Данные передаем как строку
        file: http.file(fileData, 'abc.txt', 'text/plain'),
    };

    let params = {
        headers: {
            'Authorization': 'Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhYmNAYWJjLnJ1IiwiZXhwIjoxNzQwNzM5ODI4LCJpYXQiOjE3NDA3MzYyMjgsInJvbGVzIjpbeyJpZCI6MSwicm9sZSI6IkFETUlOIn0seyJpZCI6Miwicm9sZSI6IlVTRVIifV19.Sp_uY0n5FEeELuwQ6PfsS9A9l2wP3saNRiiD7eyg-nQ'
        }
    };

    let res = http.post(url, formData, params);
    check(res, {
        'status was 200': (r) => r.status == 200
    });

    sleep(1);
}