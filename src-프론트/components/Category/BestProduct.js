import React, { useEffect, useState } from 'react';
import '../../css/Category.css';
import  axios  from 'axios';
import defaultFileImg from '../../images/defaultFileImg.png';

export const BestProduct = () => {
  const [bestProducts, setBestProducts] = useState([]);

  useEffect(() => {
    const fetchBestProducts = async () => {
      try {
        const response = await axios.get('http://localhost:8080/auction/top'); // URL을 필요에 따라 조정하세요
        console.log(response);
        if (!response.statusMessage === 'ok') {
          throw new Error('데이터를 가져오는 데 실패했습니다.');
        }
        const data = response.data;
        console.log(data.pageItems.content);
        setBestProducts(data.pageItems.content); // pageItems에 제품 데이터가 있다고 가정
      } catch (error) {
        console.error('베스트 상품을 가져오는 중 오류 발생:', error);
      }
    };

    fetchBestProducts();
  }, []);

  return (
    <div className='CTG_container2'>
      <div className='CTG_grid-container-best'>
        {
        bestProducts.map((auction, index) => {
            const thumbnailImage = auction.auctionImageDtoList.find(image => image.thumbnail === true);
            const imageSrc = thumbnailImage && thumbnailImage.filetype === 'image'
              ? `https://kr.object.ncloudstorage.com/bitcamp119/${thumbnailImage.filepath}${thumbnailImage.filename}`
              : `${defaultFileImg}`;  // 이미지가 없거나 썸네일이 아닐 경우 기본 이미지

          return (
            <div key={index} className="CTG_grid-item-best">

                <img 
                className='CTG_grid-img'
                src={imageSrc} alt={auction.productName} />
            </div>  
          )

        })
      }

      </div>
    </div>
  );
};